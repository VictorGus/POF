(ns ui.patient-workflow.model
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-sub
 ::patient-data
 (fn [db _]
   (::patients db)))

(rf/reg-event-fx
 ::search
 (fn [{db :db} [pid params]]
   {:xhr/fetch {:uri (str "/search/" (str/replace params #" " "%20"))
                :req-id (or pid "pid")
                :success {:event ::save-results}}}))

(rf/reg-event-fx
 ::save-results
 (fn [{db :db} [_ {data :data}]]
   {:db (assoc db ::patients data)}))

