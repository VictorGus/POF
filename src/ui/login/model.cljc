(ns ui.login.model
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.login.form :as form]
            [ui.pages :as pages])
  #?(:clj (:import java.util.Base64)))

(def index ::login)

(rf/reg-event-fx
 ::sign-in
 (fn [{db :db} [pid]]
   (let [encoded #?(:clj  (String. (.encodeToString (Base64/getEncoder) (get-in db [form/form-path :password])))
                    :cljs (js/btoa (get-in db [form/form-path :password])))]
     {:xhr/fetch  {:uri"/Login/"
                   :body {:login (get-in db [form/form-path :login])
                          :password encoded}
                   :method "POST"
                   :success {:event ::success-redirect}
                   :req-id index}})))

(rf/reg-event-fx
 ::success-redirect
 (fn [{db :db} [_ {{:keys [jwt]} :data}]]
   {:ui.zframes.cookies/set {:key   :jwt
                             :value jwt}
    :ui.zframes.redirect/redirect-with-refresh {:url (get db :sign-in-redirect)}}))

(rf/reg-event-fx
 ::sign-out
 (fn [{db :db} _]
   {:ui.zframes.cookies/remove :jwt
    :ui.zframes.redirect/redirect-with-refresh {:url "/login"}}))

(rf/reg-event-fx
 index
 (fn [{db :db} [pid phase params]]
   {}))

(rf/reg-sub
 index
 (fn [db]
   {}))

