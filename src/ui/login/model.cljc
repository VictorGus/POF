(ns ui.login.model
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.pages :as pages]))

(def index ::login)

(rf/reg-event-fx
 index
 (fn [{db :db} [pid phase params]]
   {}))

(rf/reg-sub
 index
 (fn [db]
   {}))

