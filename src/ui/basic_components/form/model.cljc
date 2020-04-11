(ns ui.basic-components.form.model
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
 ::form-set-value
 (fn [{db :db} [_ {:keys [path value]}]]
   {:db (assoc db path value)}))
