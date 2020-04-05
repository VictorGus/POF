(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]))

(def index-card ::index-card)

(rf/reg-event-fx
 index-card
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/patient/" (get-in db [:route-map/current-route :params :uid]))
                :req-id index-card}}))

(rf/reg-event-fx
 ::save-results
 (fn [{db :db} [_ {data :data}]]
   {:db (assoc db ::patient data)}))

(rf/reg-sub
 index-card
 :<- [:pages/data   index-card]
 :<- [:xhr/response index-card]
 (fn [[page {resp :data}] [pid]]
   (merge page {:data resp})))
