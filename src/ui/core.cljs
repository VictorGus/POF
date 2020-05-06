(ns ^:figwheel-hooks ui.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.zframes.xhr]
            [ui.zframes.redirect]
            [ui.zframes.flash :as flash]
            [ui.routes :as routes]
            [ui.pages :as pages]
            [ui.layout :as layout]
            [ui.patient-workflow.views]
            [ui.patient-workflow.card.view]
            [ui.audit.logs.view]))

(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx :window-location)]
 (fn [{location :location db :db} _]
   {:db (-> db
            (assoc-in [:xhr :config :base-url] "http://localhost:9090" )
            (assoc :route-map/routes routes/routes))
    :route-map/start {}}))

(defn not-found-page []
  [:h1 "Not found"])

(defn current-page []
  (let [route  (rf/subscribe [:route-map/current-route])]
    (fn []
      (let [page (get @pages/pages (:match @route))
            params (:params @route)]
        [layout/layout
         (if page
           [page params]
           [not-found-page])]))))

(defn mount-root []
  (rf/dispatch-sync [::initialize])
  (r/render [current-page] (.getElementById js/document "app")))

(defn ^:after-load re-render [] (mount-root))
