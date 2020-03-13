(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]))

(def index-card ::index-card)

index-card

(rf/reg-event-fx
 index-card
 (fn [{db :db} [pid phase params]]
   {:db db}))

(rf/reg-sub
 index-card
 (fn [_ _]
   {}))

