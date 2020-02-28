(ns ui.pages
  (:require [re-frame.core :as rf]))

(defonce pages (atom {}))

(rf/reg-sub
 :pages/get-in
 (fn [db [_ path]]
   (get-in db path)))

(rf/reg-sub
 :pages/data
 (fn [db [_ pid]]
   (get db pid)))

(defn reg-page [key page]
  (swap! pages assoc key page))

(defn subscribed-page [page-idx view]
  (fn [params]
    (let [m (rf/subscribe [page-idx])]
      (fn [] [view @m params]))))

(defn reg-subs-page
  "register subscribed page under keyword for routing"
  [key f & [layout-key]]
  (swap! pages assoc key (subscribed-page key f)))

(rf/reg-sub
 :config
 (fn [db path]
   (get-in db path)))
