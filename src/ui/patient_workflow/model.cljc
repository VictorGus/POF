(ns ui.patient-workflow.model
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

#_(defn parse-date [date]
  (.parse (js/Date  date)))

(rf/reg-sub
 ::patient-data
 (fn [db _]
   (::patients db)))

(defn sort-by-birthdate [sort-order data]
  (println sort-order)
  (vec (sort (fn [current next]
               (let [c #?(:clj (inst-ms (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") (:birthDate current)))
                          :cljs (.parse js/Date (:birthDate current)))
                     n #?(:clj (inst-ms (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") (:birthDate next)))
                          :cljs (.parse js/Date. (:birthDate next)))]
                 (if sort-order
                   (< c n)
                   (> c n)))) data)))

(rf/reg-event-fx
 ::sort-patients
 (fn [{db :db} [_ params sort-order]]
   {:db (update db ::patients (partial sort-by-birthdate sort-order))}))

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

