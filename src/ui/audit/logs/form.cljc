(ns ui.audit.logs.form
  (:require [re-frame.core :as rf]))

(def form-path ::logs)
(def filter ::logs-filter)

(rf/reg-event-fx
 ::search-user
 (fn [{db :db} [_]]
   {:xhr/fetch {:uri      "/Users/search"
                :success  {:event  ::users-loaded}}}))

(rf/reg-sub
 ::users
 (fn [db _]
   (get-in db [path :user :items])))

(rf/reg-event-fx
 ::users-loaded
 (fn [{db :db} [_ {data :data}]]
   {:db (assoc-in db [path :user :items] (mapv (fn [el]
                                                 {:display (:name (:resource el))
                                                  :value (:id el)}) data))}))
