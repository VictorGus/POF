(ns ui.zframes.flash
  (:require [ui.styles :as s]
            [re-frame.core :as rf]))


(defn gen-uuid []
  #?(:clj (java.util.UUID/randomUUID)
     :cljs (random-uuid)))

(rf/reg-event-db
 ::flash
 (fn [db [_ status {:keys [id msg time] :as opts}]]
   #?(:cljs (let [id (or id (keyword (str (gen-uuid))))]
              (js/setTimeout #(rf/dispatch [::remove-flash id]) (or time 8000))
              (assoc-in db [:flash id] {:st status :msg msg}))
      :clj  (let [id (or id (keyword (str (gen-uuid))))]
              (assoc-in db [:flash id] {:st status :msg msg})))))

(rf/reg-event-db
 ::add-flash
 (fn [db [_ {:keys [status id msg] :as opts}]]
   (assoc-in db [:flash id] {:st status :msg msg})))

(rf/reg-event-fx
 ::remove-flash
 (fn [{db :db} [_ id]]
   {:db (update db :flash dissoc id)}))

(rf/reg-fx
 :flash/flash
 (fn [ [status opts]]
   (rf/dispatch [::flash status opts])))

(rf/reg-event-fx :flash/success   (fn [_ [_ opts]] {:flash/flash [:success      opts]}))
(rf/reg-event-fx :flash/danger    (fn [_ [_ opts]] {:flash/flash [:danger       opts]}))
(rf/reg-event-fx :flash/warning   (fn [_ [_ opts]] {:flash/flash [:warning      opts]}))
(rf/reg-event-fx :flash/primary   (fn [_ [_ opts]] {:flash/flash [:primary      opts]}))
(rf/reg-event-fx :flash/light     (fn [_ [_ opts]] {:flash/flash [:light        opts]}))
(rf/reg-event-fx :flash/dark      (fn [_ [_ opts]] {:flash/flash [:dark         opts]}))
(rf/reg-event-fx :flash/secondary (fn [_ [_ opts]] {:flash/flash [:secondary    opts]}))
(rf/reg-event-fx :flash/info      (fn [_ [_ opts]] {:flash/flash [:info         opts]}))

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
