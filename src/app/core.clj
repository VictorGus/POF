(ns app.core
  (:require [clojure.java.io :as io]
            [route-map.core :as rm]
            [cheshire.core :as json]
            [app.p-log.search :as l]
            [app.operations :as ops]
            [app.action :as action]
            [app.crud :as crud]
            [app.p-log.core :as plog]
            [app.p-log.es-appender :as es]
            [app.manifest :as m]
            [clojure.core.async :refer [go]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [org.httpkit.server :as server]
            [clojure.string :as str])
  (:import [java.io File]))

(def routes
  {"Patient" {"search"  {:GET    ops/patients-search}
              :POST              crud/r-create
              [:params] {:GET    crud/r-read
                         :PUT    crud/r-update
                         :DELETE crud/r-delete
                         "ehr"   {:GET ops/patient-ehr}}}
   "Logs"    {:GET  l/logs-search}
   "Bulk"    {:POST ops/bulk-import}
   "Users"   {"search"  {:GET action/get-users}}
   "Login"   {:POST action/log-in}})

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
  (fn [{headers :headers uri :uri :as req}]
    (let [req-time (System/currentTimeMillis)]
      (if (= :options (:request-method req))
        (preflight req)
        (let [token (some-> (:authorization (clojure.walk/keywordize-keys headers))
                            (clojure.string/replace #"Bearer " ""))
              resp  (try
                      (cond
                        (#{"/Login/"} uri)
                        (dispatch req)
                        (action/verify-token token)
                        (dispatch req)
                        :else
                        {:status 401
                         :body {:message "Access denied"}})
                      (catch Exception e {:status 401
                                          :body {:message "Access denied"}}))]
          (go (plog/log req resp (- (System/currentTimeMillis) req-time)))
          (-> resp (allow req)))))))

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
  (go (action/create-users))
  (go (action/create-clients))
  (reset! state (server/run-server app {:port 9090})))

(defn restart-server [] (stop-server) (start-server))

(comment
  (restart-server)
  (es/es-appender {:es-url (get-in m/manifest [:config :elastic :host])
                   :batch-size 2})


  )
