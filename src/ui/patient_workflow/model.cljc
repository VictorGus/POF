(ns ui.patient-workflow.model
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::patient-data
 (fn [db _]
   [{:name "Test Test1" :birthDate "19.11.1960" :address "Test st." :gender "male"}
    {:name "Test Test2" :birthDate "12.01.1970" :address "Test2 st." :gender "female"}])) ;;TODO

