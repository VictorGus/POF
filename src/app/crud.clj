(ns app.crud
  (:require [app.dbcore :refer [run-query]]
            [honeysql.core :as hsql]
            [clojure.string :as str]
            [cheshire.core :as json]
            [app.validator :as validator]
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

(defn r-update [{body :body {params :params} :params :as request} & [skip-validation]]
  (let [body  (cond-> body
                (not (map? body))
                (-> slurp json/parse-string walk/keywordize-keys))
        resource (assoc (u/deep-merge (-> (r-read params)
                                          walk/keywordize-keys
                                          (get-in [:body :entry])
                                          first
                                          :resource) body) :id (or params (:id body)))
        query    {:select [(hsql/call :fhirbase_create
                                      (hsql/raw (str "'" (-> resource
                                                             json/generate-string
                                                             (str/replace #"'" "")) "'")))]}
        [{query-result :fhirbase_create}] (-> query
                                              hsql/format
                                              run-query)]
    (cond
      (and
       (not skip-validation)
       (not-empty (:errors (validator/validate-resource resource))))
      {:status 404
       :body (validator/validate-resource resource)}
      :else
      {:status 200 :body (:fhirbase_create (first (-> query
                                                      hsql/format
                                                      run-query)))})))

(defn r-create [{body :body :as request} & [id skip-validation]]
  (let [parsed-body  (cond-> body
                       (not (map? body))
                       (-> slurp json/parse-string))
        body (-> parsed-body
                 walk/keywordize-keys
                 (cond->
                     (not (:resourceType parsed-body))
                   (assoc :resourceType "Patient"))
                 (cond->
                     (not (:id parsed-body))
                   (assoc :id (or id (str (java.util.UUID/randomUUID))))))
        query {:select [(hsql/call :fhirbase_create (hsql/raw (str "'" (str/replace (-> body u/remove-nils json/generate-string) #"'" "") "'")))]}]
    (println (hsql/format query))
    (cond
      (and
       (not skip-validation)
       (not-empty (:errors (validator/validate-resource body))))
      {:status 404
       :body (validator/validate-resource body)}
      :else
      {:status 201
       :body (:fhirbase_create (first (-> query
                                          hsql/format
                                          run-query)))})))
