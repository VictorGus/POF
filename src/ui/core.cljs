(ns ^:figwheel-hooks ui.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.zframes.xhr]
            [ui.zframes.redirect :as redirect]
            [ui.zframes.flash :as flash]
            [ui.zframes.cookies :as cookies]
            [ui.routes :as routes]
            [ui.pages :as pages]
            [ui.layout :as layout]
            [ui.patient-workflow.views]
            [ui.patient-workflow.card.view]
            [ui.login.view]
            [ui.audit.logs.view]))

(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx :window-location)]
 (fn [{location :location db :db} _]
   {:db (cond-> (-> db
                    (assoc-in [:xhr :config :base-url] "http://localhost:9090")
                    (assoc :route-map/routes routes/routes))
          (not= (:href location) "/login")
          (assoc :sign-in-redirect (:hash location))
          (cookies/get-cookie :jwt)
          (assoc-in [:xhr :config :token] (cookies/get-cookie :jwt)))
    :route-map/start {}}))

(defn not-found-page []
  [:h1 "Not found"])

(defn current-page []
  (let [route  (rf/subscribe [:route-map/current-route])]
    (fn []
      (let [page (get @pages/pages (:match @route))
            params (:params @route)]
        (cond
          (and (not (= (:match @route)
                       :ui.login.model/login)) (cookies/get-cookie :jwt))
          [layout/layout
           (if page
             [page params]
             [not-found-page])]

          (= (:match @route) :ui.login.model/login)
          [(get @pages/pages :ui.login.model/login) params]

          (not (cookies/get-cookie :jwt))
          (redirect/redirect "/login"))))))

(defn mount-root []
  (rf/dispatch-sync [::initialize])
  (r/render [current-page] (.getElementById js/document "app")))

(defn ^:after-load re-render [] (mount-root))
