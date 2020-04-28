(ns app.crud
  (:require [app.dbcore :refer [run-query]]
            [honeysql.core :as hsql]
            [clojure.string :as str]
            [cheshire.core :as json]
            [clojure.walk :as walk]
            [honeysql.core :as hsql]
            [honeysql.helpers :refer :all]
            [honeysql-postgres.format :refer :all]
            [app.utils :as u]))

(defn r-read [params]
  (let [id (if (map? params)
             (get-in params [:params :params])
             params)]
    {:status 200
     :body {:entry (run-query (hsql/format {:select [:resource]
                                            :from [:patient]
                                            :where [:= :id id]}))}}))

(defn r-delete [params]
  (let [id (if (map? params)
             (get-in params [:params :params])
             params)]
    (-> {:select [(hsql/call :fhirbase_delete
                             "Patient" id)]}
        hsql/format
        run-query)
    {:status 200
     :body "ok"}))

(defn r-update [{body :body {params :params} :params :as request}]
  (let [body  (cond-> body
                (not (map? body))
                (-> slurp json/parse-string walk/keywordize-keys))
        resource (assoc (u/deep-merge (-> (r-read params)
                                          walk/keywordize-keys
                                          (get-in [:body :entry])
                                          first
                                          :resource) body) :id (or params (:id body)))
        [{query-result :fhirbase_create}] (-> {:select [(hsql/call :fhirbase_create
                                                                   (hsql/raw (str "'" (-> resource
                                                                                          json/generate-string
                                                                                          (str/replace #"'" "")) "'")))]}
                                              hsql/format
                                              run-query)]
    {:status 200 :body query-result}))

(defn r-create [{body :body :as request} & [id]]
  (let [parsed-body  (cond-> body
                       (not (map? body))
                       (-> slurp json/parse-string))
        body (-> parsed-body
                 (assoc :resourceType "Patient")
                 (cond->
                     (not (:id parsed-body))
                   (assoc :id (or id (str (java.util.UUID/randomUUID)))))
                 json/generate-string
                 u/remove-nils)
        query {:select [(hsql/call :fhirbase_create (hsql/raw (str "'" (str/replace body #"'" "") "'")))]}
        [{result :fhirbase_create}] (-> query
                                        hsql/format
                                        run-query)]
    {:status 201
     :body result}))
