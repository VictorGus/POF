(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]
            [ui.helper :as helper]
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
 ::add-item
 (fn [{db :db} [_ path]]
   {:db (update-in db [:xhr :req index-card :data :patient 0 path] conj {})}))

(rf/reg-event-fx
 ::remove-item
 (fn [{db :db} [_ path]]
   {:db (update-in db [:xhr :req index-card :data :patient 0 (first path)] helper/vec-remove (second path))}))

(rf/reg-event-fx
 edit
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/Patient/" (get-in db [:route-map/current-route :params :uid]) "/ehr")
                :req-id index-card}}))

(rf/reg-sub
 edit
 :<- [:pages/data   edit]
 :<- [:xhr/response index-card]
 (fn [[page {resp :data}] [pid]]
   (rf/dispatch [::form/init])
   (merge page {:data resp})))
