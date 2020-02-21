(ns app.core
  (:require [org.httpkit.client :as http]
            [route-map.core :as rm]
            [clj-yaml.core :as yaml]
            [app.auth])
  (:use org.httpkit.server))

(def routes
  {:GET #'signup-handler
   "info" {:GET #'client-info-handler}
   "diag" {:GET #'get-diagnosis}})

(defn dispatch [{meth :request-method uri :uri :as req}]
  (if-let [{handler :match params :params} (rm/match [meth uri] #'routes)]
    (handler (assoc req :route-params params))))

(comment

  (def plain (clojure.string/split-lines (slurp "/home/victor/Documents/Pet-Projects/univs.txt")))

  (spit "/home/victor/Documents/Pet-Projects/univs.yaml" (yaml/generate-string (map
                                                                                (fn [el]
                                                                                  (let [m (zipmap [:name :rankingPlace :country :city :coords :site :studyProgramms] el)]
                                                                                    (-> m
                                                                                        (assoc :studyProgramms (clojure.string/split (:studyProgramms m) #","))
                                                                                        (assoc :coords (clojure.string/split (clojure.string/replace (:coords m) " " "") #",")))))
                                                                                (partition 7 (map #(clojure.string/replace % "\t" "") (filter #(not (clojure.string/blank? %))plain))))))
  
  (def ser (run-server #'dispatch {:port 5555}))

  (ser))


