(ns ui.patient-workflow.model
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::patient-data
 (fn [db _]
   [{:name "Test Test1" :birthDate "19.11.1960" :address "Test st." :gender "male"}
    {:name "Test Test2" :birthDate "12.01.1970" :address "Test2 st." :gender "female"}])) ;;TODO

(rf/reg-event-fx
 ::test
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri "/search/1,2"
                :req-id "pid"
                :success {:event ::test-another}}}))

(rf/reg-event-fx
 ::test-another
 (fn [{db :db}]
   (println "test")))
