(ns ui.patient-workflow.card.form
  (:require [re-frame.core :as rf]
            [ui.basic-components.form.model :as basic-form]))

(def form-path ::edit)

(def form-schema
  {:gender     {:type "string"}
   :telecom    {:type "array"
                :use    {:type "string"}
                :value  {:type "string"}
                :system {:type "string"}}
   :name       {:type "array"
                :family {:type "string"}
                :given  {:type "array"}}
   :address    {:type "array"
                :city       {:type "string"}
                :country    {:type "string"}
                :state      {:type "string"}
                :postalCode {:type "string"}}
   :identifier {:type "array"}})

(rf/reg-event-fx
 ::init
 (fn [{db :db} _]
   {:dispatch [::basic-form/init-form form-path form-schema]}))

(rf/reg-event-fx
 ::eval
 (fn [{db :db} _]
   {:dispatch [::basic-form/eval-form form-path]}))
