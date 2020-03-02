(ns ui.patient-workflow.model
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-sub
 ::patient-data
 (fn [db _]
   [{:name "Test Test1" :birthDate "19.11.1960" :address "Test st." :gender "male"}
    {:name "Test Test2" :birthDate "12.01.1970" :address "Test2 st." :gender "female"}])) ;;TODO

(rf/reg-event-fx
 ::search
 (fn [{db :db} [pid params]]
   {:xhr/fetch {:uri (str "/search/" (str/replace params #" " "%20"))
                :req-id (or pid "pid")
                :success {:event ::test-another}}}))

(rf/reg-event-fx
 ::test-another
 (fn [{db :db} [_ data]]
   nil))
