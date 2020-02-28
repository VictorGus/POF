(ns ui.zframes.xhr
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [clojure.string :as str]
            [re-frame.db :as db]
            [ui.zframes.redirect]
            [re-frame.core :as rf]))

(defn sub-query-by-spaces
  [k s] (->> (str/split s #"\s+")
             (mapv (fn [v] (str (name k) "=" v)))
             (str/join "&")))

(defn to-query [params]
  (->> params
       (reduce-kv (fn [acc k v]
                    (if (or (nil? v) (str/blank? v))
                      acc
                      (assoc acc k v)))
                  {})
       (mapcat (fn [[k v]]
                 (cond
                   (vector? v) (mapv (fn [vv] (str (name k) "=" vv)) v)
                   (set? v) [(str (name k) "=" (str/join "," v))]
                   :else [(str (name k) "=" v) #_(sub-query-by-spaces k v)])))
       (str/join "&")))

(defn base-url [db url]
  (str (get-in db [:config :base-url]) url))

(defn make-form-data [files]
  (let [form-data (js/FormData.)]
    (doall
     (for [[i file] (map-indexed vector files)]
       (.append form-data (str "file" i) file (str "file" i))))
    form-data))

(defonce abort-controller-cache (atom {}))

(defn get-abort-controller [req-id]
  (when-let [ctrl (get @abort-controller-cache req-id)]
    (.abort ctrl))
  (swap! abort-controller-cache assoc req-id (js/AbortController.))
  (get @abort-controller-cache req-id))

(defn *json-fetch [{:keys [req-id uri format headers cookies is-fetching-path params success error] :as opts}]
  (let [{:keys [token base-url x-correlation-id]} (get-in @db/app-db [:xhr :config])
        screen (get-in @db/app-db [:route-map/current-route :match])
        abort-controller (when req-id (get-abort-controller req-id))
        fmt (or (get {"json" "application/json" "yaml" "text/yaml"} format) "application/json")
        x-audit (some->> (get-in @db/app-db [:app.global-context/organization :main-organization :id])
                         (hash-map :morg-id)
                         clj->js
                         (.stringify js/JSON)
                         js/btoa)
        headers (cond-> {"accept"        fmt
                         "x-correlation-id" x-correlation-id 
                         "authorization" (str "Bearer " token)}
                  x-audit                              (assoc "x-audit" x-audit)
                  (or (nil? token) (str/blank? token)) (dissoc "authorization")
                  (nil? (:files opts))                 (assoc "Content-Type" "application/json")
                  true                                 (merge (or headers {})))
        fetch-opts (-> (merge {:method "get" :mode "cors" :credentials "same-origin"}
                              (when abort-controller {:signal (.-signal abort-controller)})
                              opts)
                       (dissoc :uri :headers :success :error :params :files)
                       (assoc :headers headers))
        fetch-opts (cond-> fetch-opts
                     (:body opts) (assoc :body (if (string? (:body opts)) (:body opts) (.stringify js/JSON (clj->js (:body opts)))))
                     (:files opts) (assoc :body (make-form-data (:files opts))))
        url (str base-url uri)]

    (when is-fetching-path (rf/dispatch [::fetch-start is-fetching-path]))

    (->
     (js/fetch (str url (when params (str "?" (to-query params)))) (clj->js fetch-opts))
     (.then
      (fn [resp]
        (when is-fetching-path (rf/dispatch [::fetch-end is-fetching-path]))
        (if  (= 500 (.-status resp))
          (throw resp)
          (if (:dont-parse opts)
            (.then (.text resp)
                   (fn [doc]
                     (let [e (if (<= (.-status resp) 299) success error)]
                       (rf/dispatch [(:event e) (merge e {:request opts, :data doc})])))
                   ;; No json
                   (fn [doc]
                     (println "Error:" doc)
                     (rf/dispatch [(:event success) (merge success {:request opts :data doc})])))
            (.then (.json resp)
                   (fn [doc]
                     (let [data (js->clj doc :keywordize-keys true)]
                       (->> [(when req-id
                               [:xhr/done {:request opts :data data :status (.-status resp)}])
                             (when-let [e (if (< (.-status resp) 299) success error)]
                               [(:event e) {:request opts :data data :status (.-status resp)} (:params e)])
                             (when (and (> (.-status resp) 299)
                                        #_(not (false? (:flash error))))
                               (let [errors  (->> data
                                                  :issue (map (fn [e] (str (:expression e) " - " (:diagnostics e)  ))))]
                                 (when-not (:flash-disabled opts)
                                   [:flash/danger
                                    {:msg  [:div
                                            [:div "Ошибка: " [:b (.-status resp)] " " (.-statusText resp)]
                                            (if-let [msg (:message data)]
                                              [:div msg]
                                              (case (.-status resp)
                                                404 [:div "Не верный адрес запроса " url]
                                                409 [:div "Конфликт с текущим состоянием сервера"]
                                                422 [:div "Не валидный запрос"]
                                                nil))
                                            (when (and (not (empty? errors)) (= 422 (.-status resp)))
                                              [:ul (for [e errors] ^{:key e} [:li e])])]}])))]
                            (mapv #(when % (rf/dispatch %))))))
                   ;; No json
                   (fn [doc]
                     (println "Error:" doc)
                     (rf/dispatch [(:event success) (merge success {:request opts :data doc})])))))))
     (.catch (fn [err]
               (when-not (= (.-name err) "AbortError")
                 (when-not (:flash-disabled opts)
                   (rf/dispatch
                    [:flash/danger
                     {:msg  [:div
                             [:div "Ошибка: " [:b (.-status err)] " " (.-statusText err)]
                             (case (.-status err)
                               500 [:div "Внутренняя ошибка сервера"]
                               404 [:div "Не верный адрес запроса"]
                               422 [:div "Не валидный запрос"]
                               [:div "Неопознанная ошибка"])
                             ]}]))
                 (rf/dispatch [(:event error) (merge error {:request opts :error err})])))))))



(defn json-fetch [opts]
  (if (vector? opts)
    (doseq [o opts] (*json-fetch o))
    (*json-fetch opts)))

(rf/reg-fx ::json-fetch json-fetch)
(rf/reg-fx :json/fetch json-fetch)
(rf/reg-fx :xhr/fetch #(rf/dispatch [:xhr/fetch %]))

(rf/reg-event-fx
 :xhr/fetch
 (fn [{db :db} [_ opts]]
   {:db (reduce (fn [acc opt]
                  (assoc-in acc [:xhr :req (:req-id opt) :loading] true))
                db
                (if (vector? opts) opts [opts]))
    :json/fetch opts}))

(rf/reg-event-fx
 :xhr/done
 (fn [{db :db} [_ {{:keys [req-id] :as req} :request :as resp}]]
   {:db (assoc-in db [:xhr :req req-id] resp)}))

(rf/reg-event-fx
 :xhr/remove-response
 (fn [{db :db} [_ req-id]]
   {:db (update-in db [:xhr :req] dissoc  req-id)}))

(rf/reg-sub
 :xhr/response
 (fn [db [_ req-id]]
   (get-in db [:xhr :req req-id])))

(rf/reg-event-fx
 :xhr/redirect
 (fn [_ [_ _ opts]]
   {:dispatch [:zframes.redirect/redirect opts]}))

(rf/reg-event-db
 ::fetch-start
 (fn [db [_ path]]
   (assoc db path true)))

(rf/reg-event-db
 ::fetch-end
 (fn [db [_ path]]
   (assoc db path false)))

(rf/reg-sub
 :xhr/config
 (fn [db _]
   (get-in db [:xhr :config])))
