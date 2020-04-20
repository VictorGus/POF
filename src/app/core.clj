(ns app.core
  (:require [clojure.java.io :as io]
            [route-map.core :as rm]
            [cheshire.core :as json]
            [app.operations :as ops]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [org.httpkit.server :as server]
            [clojure.string :as str])
  (:import [java.io File]))

(def routes
  {"Patient" {"search" {:GET ops/patients-search}
              [:params] {:GET ops/patient-by-id
                         :PUT ops/patient-update
                         "ehr" {:GET ops/patient-ehr}
                         "edit" {:PUT ops/patient-create}}}})

(defn params-to-keyword [params]
  (reduce-kv (fn [acc k v]
               (assoc acc (keyword k) v))
             {} params))

(defn handler [{meth :request-method uri :uri :as req}]
  (if-let [res (rm/match [meth uri] routes)]
    ((:match res) (-> (assoc req :params (params-to-keyword (:params req)))
                      (update-in [:params] merge (:params res))))
    {:status 404 :body {:error "Not found"}}))

(defn preflight
  [{meth :request-method hs :headers :as req}]
  (let [headers (get hs "access-control-request-headers")
        origin (get hs "origin")
        meth  (get hs "access-control-request-method")]
    {:status 200
     :headers {"Access-Control-Allow-Headers" headers
               "Access-Control-Allow-Methods" meth
               "Access-Control-Allow-Origin" origin
               "Access-Control-Allow-Credentials" "true"
               "Access-Control-Expose-Headers" "Location, Transaction-Meta, Content-Location, Category, Content-Type, X-total-count"}}))

(defn allow [resp req]
  (let [origin (get-in req [:headers "origin"])]
    (update resp :headers merge
            {"Access-Control-Allow-Origin" origin
             "Access-Control-Allow-Credentials" "true"
             "Access-Control-Expose-Headers" "Location, Content-Location, Category, Content-Type, X-total-count"})))

(defn mk-handler [dispatch]
  (fn [req]
    (if (= :options (:request-method req))
      (preflight req)
      (let [resp (dispatch req)]
        (-> resp (allow req))))))

(def app
  (-> handler
      mk-handler
      wrap-params
      wrap-json-response
      wrap-reload))

(defonce state (atom nil))

(defn stop-server []
  (when-not (nil? @state)
    (@state :timeout 100)
    (reset! state nil)))

(defn start-server []
  (reset! state (server/run-server app {:port 9090})))

(defn restart-server [] (stop-server) (start-server))

(comment
  (restart-server)

  )
