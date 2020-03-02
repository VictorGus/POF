(ns ^:figwheel-hooks ui.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.zframes.xhr]
            [route-map.core :as route-map]
            [ui.patient-workflow.views :as upw]))

(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx :window-location)]
 (fn [{location :location db :db} _]
   {:db(-> db
            (assoc-in [:xhr :config :base-url] "http://localhost:9090" )
            #_(assoc :route-map/routes routes/routes)) ;; TODO 
    :route-map/start {}}))

(defn not-found-page []
  [:h1 "Not found"])

;; TODO
#_(defn current-page []
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
  (r/render [upw/search-input] (.getElementById js/document "app")))

(defn ^:after-load re-render [] (mount-root))
