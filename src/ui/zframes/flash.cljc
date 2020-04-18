(ns ui.zframes.flash
  (:require [ui.styles :as s]
            [re-frame.core :as rf]))

(defn gen-uuid []
  #?(:clj (java.util.UUID/randomUUID)
     :cljs (random-uuid)))

(rf/reg-event-db
 ::flash
 (fn [db [_ status data-or-params maybe-params]]
   (let [{:keys [id msg time] :as opts
          :or   {id   (keyword (str (gen-uuid)))
                 time 8000}}
         (or maybe-params data-or-params)]
     #?(:cljs (do
                (js/setTimeout #(rf/dispatch [::remove-flash id]) time)
                (assoc-in db [:flash id] {:st status :msg msg}))
        :clj  (assoc-in db [:flash id] {:st status :msg msg})))))

(rf/reg-event-db
 ::add-flash
 (fn [db [_ {:keys [status id msg] :as opts}]]
   (assoc-in db [:flash id] {:st status :msg msg})))

(rf/reg-event-db ::remove-flash (fn [db [_ id]] (update db :flash dissoc id)))

(rf/reg-fx :flash/flash (fn [[status & args]] (rf/dispatch (apply vector ::flash status args))))

(doseq [type [:success :danger :warning :primary :light :dark :secondary :info]]
  (let [ev (keyword "flash" (name type))]
    (rf/reg-event-fx ev (fn [_ [_ & args]] {:flash/flash (vec (cons type args))}))))

(rf/reg-sub ::flashes (fn [db _] (:flash db)))

(defn flash-msg [id f]
  [:div.alert.alert-dismissible {:class (str "alert-" (name (:st f)))}
   [:button.close
    {:on-click #(rf/dispatch [::remove-flash id])}
    "×" ]
   (:msg f)])

(def styles
  (s/style
   [:.flashes {:position "fixed" :top "20px" :right "20px" :max-width "500px" :z-index 200}
    [:ul {:padding-left "20px"}]]))

(defn flashes []
  (let [flashes (rf/subscribe [::flashes])]
    (fn []
      (into [:div.flashes]
            (reduce-kv (fn [acc k f]
                         (conj acc (flash-msg k f)))
                       [] @flashes)))))
