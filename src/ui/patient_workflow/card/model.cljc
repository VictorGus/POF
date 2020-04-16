(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]
            [ui.helper :as helper]
            [ui.basic-components.form.model :as basic-form]
            [ui.patient-workflow.card.form :as form]))

(def index-card ::index-card)
(def edit ::edit)

(rf/reg-event-fx
 index-card
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/Patient/" (get-in db [:route-map/current-route :params :uid]) "/ehr")
                :req-id index-card}}))

(rf/reg-sub
 index-card
 :<- [:pages/data   index-card]
 :<- [:xhr/response index-card]
 (fn [[page {resp :data}] [pid]]
   (merge page {:data resp})))

(rf/reg-event-fx
 edit
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/Patient/" (get-in db [:route-map/current-route :params :uid]) "/ehr")
                :req-id edit
                :success {:event ::form/init}}}))

(rf/reg-event-fx
 ::add-item
 (fn [{db :db} [_ path]]
   {:db (update-in db [:xhr :req edit :data :patient 0 path] conj {})}))

(rf/reg-event-fx
 ::remove-item
 (fn [{db :db} [_ path]]
   {:dispatch [::basic-form/remove-item (concat [form/form-path] path)]
    :db (update-in db [:xhr :req edit :data :patient 0 (first path)] helper/vec-remove (second path))}))

(rf/reg-event-fx
 ::apply-changes
 (fn [{db :db} _]
   {:dispatch-later [{:ms 0 :dispatch [::form/eval]}
                     {:ms 300   :dispatch [::send-data]}]}))

(rf/reg-event-fx
 ::send-data
 (fn [{db :db} _]
   {:xhr/fetch {:uri    (str "/Patient/" (get-in db [:route-map/current-route :params :uid]))
                :method "PUT"
                :params (get db form/form-path)}}))

(rf/reg-sub
 edit
 :<- [:pages/data   edit]
 :<- [:xhr/response edit]
 (fn [[page {resp :data}] [pid]]
   (merge page {:data resp})))
