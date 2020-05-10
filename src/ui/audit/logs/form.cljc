(ns ui.audit.logs.form
  (:require [re-frame.core :as rf]))

(def form-path ::logs)

(rf/reg-event-fx
 ::search-user
 (fn [{db :db} [_]]
   {:xhr/fetch {:uri      "/Users/search"
                :success  {:event  ::users-loaded}}}))

(rf/reg-sub
 ::users
 (fn [db _]
   (get-in db [form-path :user :items])))

(rf/reg-event-fx
 ::users-loaded
 (fn [{db :db} [_ {data :data}]]
   {:db (assoc-in db [form-path :user :items] (map (fn [el]
                                                     {:display (:name (:resource el))
                                                      :value (:id el)}) data))}))
