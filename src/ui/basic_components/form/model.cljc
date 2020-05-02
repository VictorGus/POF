(ns ui.basic-components.form.model
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::form-values
 (fn [db [pid path]]
   (get-in db path)))

(rf/reg-event-fx
 ::init-form
 (fn [{db :db} [_ path form-schema]]
   {:db (assoc db path {:schema form-schema})}))

(rf/reg-event-fx
 ::form-set-value
 (fn [{db :db} [_ {:keys [path value]}]]
   {:db (assoc-in db path value)}))

(rf/reg-event-fx
 ::remove-item
 (fn [{db :db} [_ path]]
   {:db (update-in db (butlast path) dissoc (last path))}))

(rf/reg-event-fx
 ::eval-form
 (fn [{db :db} [_ path]]
   (let [form-schema (get-in db [path :schema])
         form-values (dissoc (get db path) :schema)]
     {:db (assoc db path (reduce-kv
                          (fn [acc k v]
                            (if (= "array" (get-in form-schema [k :type]))
                              (assoc acc k (vec (vals v)))
                              (assoc acc k v)))
                          {}
                          form-values))})))
