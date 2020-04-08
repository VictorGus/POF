(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]))

(def index-card ::index-card)
(def edit ::edit)

(rf/reg-event-fx
 index-card
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/patient/" (get-in db [:route-map/current-route :params :uid]))
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
   {:xhr/fetch {:uri (str "/patient/" (get-in db [:route-map/current-route :params :uid]))
                :req-id index-card}}))

(rf/reg-sub
 edit
 :<- [:pages/data   edit]
 :<- [:xhr/response index-card]
 (fn [[page {resp :data}] [pid]]
   (merge page {:data resp})))
