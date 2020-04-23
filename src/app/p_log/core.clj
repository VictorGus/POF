(ns app.p-log.core
  (:require [clojure.core.async :as async]
            [chrono.now :as now]
            [chrono.core :as ch]))

(def iso-fmt [:year "-" :month "-" :day "T" :hour ":" :min ":" :sec])

(defonce logs (agent {}))
(defonce appenders (atom {}))

(defn add-appender [k f]
  (swap! appenders assoc k f))

(defn reset-appenders []
  (reset! appenders {}))

(defn log [req resp duration-mills]
  (async/thread
    (send logs assoc :request req :response resp :duration duration-mills)))

(defn mk-log-msg [{:keys [uri params body
                          request-method query-params] :as req}
                  {:keys [status] :as resp} duration-mills]
  {:st status
   :d duration-mills
   :ts (ch/format (now/local) iso-fmt)
   :l_uri uri
   :l_m request-method
   :l_q_params query-params
   :l_params params
   :l_body body})

(add-watch logs :watcher
           (fn [key agent old-value new-value]
             (doseq [f (vals @appenders)]
               (f (mk-log-msg new-value)))))
