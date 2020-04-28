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
  (let [body (walk/keywordize-keys (json/parse-string (slurp body)))
        resource (assoc (u/deep-merge (-> (r-read params)
                                          walk/keywordize-keys
                                          (get-in [:body :entry])
                                          first
                                          :resource) body) :id params)
        [{query-result :fhirbase_create}] (-> {:select [(hsql/call :fhirbase_create
                                                                   (hsql/raw (str "'" (-> resource
                                                                                          json/generate-string
                                                                                          (str/replace #"'" "")) "'")))]}
                                              hsql/format
                                              run-query)]
    {:status 200 :body query-result}))
