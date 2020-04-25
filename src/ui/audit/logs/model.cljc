(ns ui.audit.logs.model
  (:require [re-frame.core :as rf]))

(def logs ::logs)

(rf/reg-event-fx
 logs
 (fn [{db :db} [pid phase params]]
   {}))

(rf/reg-sub
 logs
 (fn [_ _]
   {}))
