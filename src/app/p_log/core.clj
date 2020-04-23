(ns app.p-log.core
  (:require [clojure.core.async :as async]
            [chrono.now :as now]
            [chrono.core :as ch]))

(def fmt (let [tz (java.util.TimeZone/getTimeZone "UTC")
               df (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")]
           (.setTimeZone df tz)
           df))

(defonce logs (agent {}))
(defonce appenders (atom {}))

(defn add-appender [k f]
  (swap! appenders assoc k f))

(defn reset-appenders []
  (reset! appenders {}))

(defn log [req resp duration-mills]
  (async/thread
    (send logs assoc :request req :response resp :duration duration-mills)))

(defn mk-log-msg [{{:keys [uri params body
                           request-method query-params]} :request
                   {:keys [status]} :response duration-mills :duration}]
  {:st status
   :d duration-mills
   :ts (.format fmt (java.util.Date.))
   :l_uri uri
   :l_m request-method
   :l_q_params query-params
   :l_params params
   :l_body body})

(add-watch logs :watcher
           (fn [key agent old-value new-value]
             (doseq [f (vals @appenders)]
               (f (mk-log-msg new-value)))))
