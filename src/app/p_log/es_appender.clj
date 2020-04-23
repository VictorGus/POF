(ns app.p-log.es-appender
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [app.p-log.core :as plog]))

(defn es-appender [{:keys [es-url index batch-size batch-timeout] :as opts}]
  (let [es-url (str es-url "/_bulk")
        batch-size    (or batch-size 20)
        batch-timeout (or batch-timeout 360000)
        start-time (atom (System/currentTimeMillis))
        index (or index "logs")
        batch (atom "")
        i (atom 0)]
    (plog/add-appender :es
     (fn [l]
       (let [line (str "{\"index\": {\"_index\": \"" (str/lower-case index) "\"}}\n"
                       (json/generate-string l) "\n")]
         (swap! batch str line)
         (swap! i inc)
         (when (or (> (- (System/currentTimeMillis) @start-time) batch-timeout)
                   (>= @i batch-size))
           (try
             (http/post es-url
                        {:headers {"Content-Type" "application/x-ndjson"}
                         :body @batch}
                        (fn [{:keys [status headers body error opts]}]
                          (when error
                            (println error))))
             (catch Exception e (do
                                  (println "error: " e (.getMessage e)))))
           (reset! batch "")
           (reset! start-time (System/currentTimeMillis))
           (reset! i 0)))))))
