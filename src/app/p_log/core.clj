(ns app.p-log.core
  (:require [clojure.core.async :as async]
            [app.utils :as u]
            [app.action :as action]
            [chrono.now :as now]
            [clojure.walk :as walk]
            [app.dbcore :refer [read-query]]
            [honeysql.core :as hsql]
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

(defn shape-display [body]
  (let [patient (walk/keywordize-keys (or (first (:patient body))
                                          body))
        name    (or (get patient :patient_name)
                    (first (get patient :name)))
        id      (:value (u/vec-search "SB" (get patient :identifier)))]
    (when (or name id)
      (str (:family name) " " (first (:given name)) " (SSN: "id")"))))

(defn get-username [id])

(defn mk-log-msg [{{:keys [uri params request-method query-params headers]} :request
                   {:keys [status body]} :response duration-mills :duration}]
  (let [uid (:sub (action/decode-jwt (some-> (:authorization (clojure.walk/keywordize-keys headers))
                                             (clojure.string/replace #"Bearer " ""))))]
    (cond-> {:st status
             :d duration-mills
             :ts (.format fmt (java.util.Date.))
             :l_uri uri
             :l_uid uid
             :l_uname (:name (read-query {:resourceType "public_user"
                                          :id uid}))
             :l_m request-method
             :l_q_params query-params
             :l_params params}
      (not (#{"/Patient/search"} uri))
      (assoc :l_body (shape-display body)))))

(add-watch logs :watcher
           (fn [key agent old-value new-value]
             (doseq [f (vals @appenders)]
               (f (mk-log-msg new-value)))))
