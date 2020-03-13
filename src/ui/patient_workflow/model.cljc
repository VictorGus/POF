(ns ui.patient-workflow.model
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(def index ::index)

(rf/reg-event-fx
 index
 (fn [{db :db} [pid phase params]]
   {:db db}))

(rf/reg-sub
 index
 (fn [_ _]
   {}))

(rf/reg-sub
 ::patient-data
 (fn [db _]
   (::patients db)))

(rf/reg-sub
 ::loading-status
 (fn [db _]
   (::loading db)))

(rf/reg-event-fx
 ::set-loading-status-false
 (fn [{db :db} _]
   {:db (assoc db ::loading false)}))

(rf/reg-event-fx
 ::set-loading-status-true
 (fn [{db :db} _]
   {:db (assoc db ::loading true)}))

(defn sort-by-birthdate [sort-order data]
  (vec (sort (fn [current next]
               (let [c #?(:clj (inst-ms (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") (:birthDate current)))
                          :cljs (.parse js/Date (:birthdate current)))
                     n #?(:clj (inst-ms (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") (:birthDate next)))
                          :cljs (.parse js/Date. (:birthdate next)))]
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
   {:dispatch [::set-loading-status-true]
    :xhr/fetch {:uri (str "/patients/search/" (str/replace params #" " "%20"))
                :req-id (or pid "pid")
                :success {:event ::save-results}}}))

(rf/reg-event-fx
 ::save-results
 (fn [{db :db} [_ {data :data}]]
   {:db (assoc db ::patients data)
    :dispatch [::set-loading-status-false]}))

