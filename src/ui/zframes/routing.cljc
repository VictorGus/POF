(ns ui.zframes.routing
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [clojure.set :as set]
            [ui.zframes.window-location :as window-location]
            [route-map.core :as route-map]))

#?(:clj (defonce window-location (atom {:search ""
                                        :hash ""})))

(defn dispatch-routes [_]
  (let [fragment #?(:cljs (.. js/window -location -hash)
                    :clj (:hash @window-location))]
    (rf/dispatch [:fragment-changed fragment])))

(rf/reg-sub
 :route-map/fragment-params
 (fn [db _]
   (:fragment-params db)))

(rf/reg-sub
 :route-map/fragment
 (fn [db _]
   (:fragment db)))

(rf/reg-sub
 :route-map/current-route
 (fn [db _]
   (:route-map/current-route db)))

(rf/reg-sub
 :route-map/fragment-path
 (fn [db _]
   (:fragment-path db)))

(rf/reg-sub
 :route-map/location-search
 (fn [db _]
   (:location/search db)))

(rf/reg-fx
 :html-title
 (fn [title]
   #?(:cljs (set! (.-title  js/document) (str  "UndeFHIR" (when title (str " - " title)))))))

(defn normalize-params [{:keys [o-params n-params]}]
  (let [op       (dissoc o-params :. :params)
        np       (-> (dissoc n-params :. :params)
                     (select-keys (keys op)))
        op       (select-keys op (keys np))]
    {:o-params op, :n-params np}))

(defn params-changed? [params]
  (let [{op :o-params, np :n-params} (normalize-params params)]
    (or (not= np op) (empty? op))))

(defn contexts-diff [route old-contexts new-contexts params old-params]
  (let [to-dispose (reduce (fn [acc [k o-params]]
                             (cond-> acc
                               (params-changed? {:o-params o-params, :n-params (get new-contexts k)})
                               (conj [k :deinit o-params])))
                           []
                           old-contexts)]
    (reduce (fn [acc [k n-params]]
              (cond-> acc
                (params-changed? {:o-params (get old-contexts k), :n-params n-params})
                (conj [k :init n-params])))
            to-dispose
            new-contexts)))

 ;(and op (= (:.. np) route))     ;; Need to rethink this
 ;(conj [k :return n-params])

(defn parse-params [s]
  (if (str/blank? s)
    {}
    (reduce
     (fn [acc pair]
       (let [[k v] (str/split pair #"=" 2)]
         (assoc acc (keyword k) #?(:cljs (js/decodeURIComponent v)
                                   :clj v))))
     {} (-> (str/replace s #"^\?" "")
            (str/split "&")))))

(defn dispatch-context [_]
  (let [query #?(:cljs (.. js/window -location -search)
                 :clj (:search @window-location))]
    (rf/dispatch [:search-changed (parse-params query)])))

(rf/reg-event-fx
 :search-changed
 (fn [{db :db} [k search]]
   (let [old    (:location/search db)
         ctx-rs (:context/routes db)
         prev-h (get ctx-rs (:context old))]

     (if (not (= old search))
       (do
         (when prev-h {:dispatch [prev-h :deinit old]
                       :db (dissoc db :navigation)})
         (if-let [handler (get ctx-rs (:context search))]
           {:dispatch [handler :init search]
            :db (assoc db :location/search search)}
           {:db (assoc db :location/search search)}))
       {:db (assoc db :location/search search)}))))

(defn parse-fragment [fragment]
  (let [[path params-str] (-> fragment
                              (str/replace #"^#" "")
                              (str/split #"\?"))
        params  (if (str/blank? params-str) {} (window-location/parse-querystring (or params-str "")))]
    {:path path
     :query-string params-str
     :params params}))


(rf/reg-event-fx
 :fragment-changed
 (fn [{db :db} [k fragment]]
   ;;(.scrollTo js/window 0 0)
   (let [{path :path q-params :params qs :query-string} (parse-fragment fragment)]
     (if-let [route (route-map/match [:. path] (:route-map/routes db))]
       (let [params (assoc (:params route) :params q-params)
             params (assoc params :search (:location/search db))
             route-match (:match route)
             route {:match route-match :params params :parents (:parents route)}
             contexts (reduce (fn [acc {ctx :context, r :.}]
                                (if ctx
                                  (assoc acc ctx (assoc params :.. r
                                                        :. route-match))
                                  acc)) {} (:parents route))
             current-page (:match route)
             old-page     (get-in db [:route-map/current-route :match])
             old-params   (get-in db [:route-map/current-route :params])

             page-ctx-events (cond (nil? old-page)
                                   (cond-> [[current-page :init params]]
                                     (seq (:params params)) (conj [current-page :params params]))

                                   (not= current-page old-page)
                                   (-> (cond-> [] old-page (conj [old-page :deinit old-params]))
                                       (conj [current-page :init params]))

                                   (= old-params params)
                                   []

                                   (= (dissoc old-params :params)
                                      (dissoc params :params))
                                   [[current-page :params params old-params]]

                                   :else
                                   [[current-page :deinit old-params] [current-page :init params]])

             old-contexts (:route/context db)
             context-evs (contexts-diff (:match route) old-contexts contexts params old-params)
             evs (vec (concat context-evs page-ctx-events ))
             db (if (not= old-page current-page)
                    (dissoc db old-page :form)
                    db)]

         {:db (-> db
                  (assoc :route/history (conj (take 4 (:route/history db)) {:route (:match route) :uri fragment})
                         :fragment fragment
                         :fragment-params params
                         :fragment-path path
                         :fragment-query-string qs
                         :route/context contexts
                         :route-map/current-route route))
          :html-title nil
          :dispatch-n evs})
       {:db (assoc db
                   :fragment fragment
                   :route-map/current-route nil
                   :route-map/current-route nil
                   :route-map/error :not-found)}))))

(rf/reg-sub
 :pop-route
 (fn [db]
   (peek (:route-stack db))))

(rf/reg-event-db
 :clear-route-stack
 (fn [db _]
   (dissoc db :route-stack)))

(defn history-fx [_]
  #?(:cljs (aset js/window "onhashchange" dispatch-routes)
     :clj (swap! window-location assoc :hash ""))
  (dispatch-routes nil))

(rf/reg-fx :history history-fx)

(defn search-history-fx [_]
  #?(:cljs (aset js/window "onpopstate" dispatch-context)
     :clj (swap! window-location assoc :search ""))
  (dispatch-context nil))

(rf/reg-fx :search-history search-history-fx)

(defn start-fx [_]
  (search-history-fx nil)
  (history-fx nil))

(rf/reg-fx :route-map/start start-fx)

(defn redirect [href]
  #?(:cljs (aset (.-location js/window) "hash" href)
     :clj (do (swap! window-location assoc :hash (if (str/blank? href)
                                                   ""
                                                   (str "#" href)))
              (dispatch-routes nil))))

(rf/reg-fx
 :route-map/redirect
 redirect)

(defn to-query-params [params]
  (->> params
       (map (fn [[k v]] (str (name k) "=" v)))
       (str/join "&")))

(defn to-hash [opts]
  (str "#" (:path opts) "?" (to-query-params (:params opts))))

(defn make-fragment [params]
  (let [opts  (parse-fragment #?(:cljs (.. js/window -location -hash)
                                 :clj (:hash @window-location)))]
    (to-hash (assoc opts :params params))))

(rf/reg-fx
 :route-map/set-params
 (fn [params]
   (let [opts  (parse-fragment #?(:cljs (.. js/window -location -hash)
                                  :clj (:hash @window-location)))
         value (to-hash (assoc opts :params params))]
     #?(:cljs (aset (.. js/window -location) "hash" value)
        :clj (do (swap! window-location assoc :hash (if (str/blank? value)
                                                      ""
                                                      (str "#" value)))
                 (dispatch-routes nil))))))
