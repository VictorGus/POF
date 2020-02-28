(ns zframes.file
  (:require [re-frame.core :as rf]
            [re-frame.db :as db]))

(defn storage-url [db url]
  (str (get-in db [:config :storage-url]) url))

(defn make-binary-file [file]
  (let [type "application/octet-stream"]
    (js/Blob. [file] (clj->js {:type type}))))

(defn *file-fetch [{:keys [uri method success error body] :as opts}]
  (let [url        (storage-url @db/app-db uri)
        fetch-opts {:method (or method "GET")
                    :headers {"Content-Type" "application/octet-stream"}
                    :body   (make-binary-file body)}]
    (->
     (js/fetch url (clj->js fetch-opts))
     (.then
      (fn [resp]
        (-> (.json resp)
            (.then
             (fn [doc]
               (let [data (js->clj doc :keywordize-keys true)]
                 (->> [(when (:req-id opts)
                         [:file/done
                          {:request opts
                           :data    data
                           :status  (.-status resp)}])
                       (when-let [e (if (<= (.-status resp) 299) success error)]
                         [(:event e)
                          {:request opts
                           :data    data}
                          (:params e)])
                       (when (and (> (.-status resp) 299)
                                  (not (false? (:flash error))))
                         (let [errors (->> data
                                           :issue (map (fn [e] (str (:expression e) " - " (:diagnostics e)  ))))]
                           [:flash/danger
                            {:msg [:div
                                   [:div "Ошибка: " [:b (.-status resp)] " " (.-statusText resp)]
                                   (if-let [msg (:message data)]
                                     [:div msg]
                                     (case (.-status resp)
                                       404 [:div "Не верный адрес запроса"]
                                       422 [:div "Не валидный запрос"]))
                                   (when (and (not (empty? errors)) (= 422 (.-status resp)))
                                     [:ul (for [e errors] ^{:key e} [:li e])])]}]))]
                      (remove nil?)
                      (mapv rf/dispatch))))
             (fn [doc]
               (println "Error:" doc)
               (rf/dispatch
                [(:event success)
                 (merge success
                        {:request opts
                         :data    doc})]))))))
     (.catch
      (fn [err]
        (.error js/console err)
        (rf/dispatch
         [:flash/danger
          {:msg [:div
                 [:div "Ошибка: " [:b (.-status err)] " " (.-statusText err)]
                 (case (.-status err)
                   404 [:div "Не верный адрес запроса"]
                   422 [:div "Не валидный запрос"])]}])
        (rf/dispatch [(:event error)
                      (merge error {:request opts :error err})]))))))

(defn file-fetch [opts]
  (if (vector? opts)
    (doseq [o opts] (*file-fetch o))
    (*file-fetch opts)))

(rf/reg-fx :file/fetch file-fetch)

(rf/reg-event-fx
 :file/upload
 (fn [_ [_ {:keys [name folder] :as req}]]
   (let [uri (str "/storage/upload/" folder "/" name)]
     {:file/fetch (-> req
                      (assoc :uri uri)
                      (dissoc :name :folder))})))

(rf/reg-event-fx
 :file/done
 (fn [{db :db} [_ {{:keys [req-id]} :request :as resp}]]
   (println resp)
   {:db (assoc-in db [:file :req req-id] resp)}))

(rf/reg-fx :file/upload (fn [req] (rf/dispatch [:file/upload req])))

(rf/reg-sub
 :file/response
 (fn [db [_ req-id]]
   (get-in db [:file :req req-id])))
