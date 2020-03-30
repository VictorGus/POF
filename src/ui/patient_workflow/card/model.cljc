(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]))

(def index-card ::index-card)

(rf/reg-event-fx
 index-card
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/patient/" (get-in db [:route-map/current-route :params :uid]))}}))

(rf/reg-sub
 index-card
 (fn [_ _]
   {}))
