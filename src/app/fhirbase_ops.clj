(ns app.fhirbase-ops
  (:require [honeysql.core :as hsql]
            [cheshire.core :as json]
            [clojure.string :as str]
            [app.utils :as u]
            [app.dbcore :refer [run-query]]))

(defn fhirbase-create [resource & [id]]
  (-> {:select [(hsql/call :fhirbase_create
                           (hsql/raw (u/wrap-apostrophes (str/replace (cond-> resource
                                                                        (and (map? resource) id)
                                                                        (-> (assoc :id id) json/generate-string)
                                                                        (map? resource)
                                                                        json/generate-string) #"'" ""))))]}
      hsql/format
      run-query))

(defn fhirbase-update [resource & [id]]
  (-> {:select [(hsql/call :fhirbase_update
                           (hsql/raw (u/wrap-apostrophes (cond-> resource
                                                           (and (map? resource) id)
                                                           (-> (assoc :id id) json/generate-string)
                                                           (map? resource)
                                                           json/generate-string))))]}
      hsql/format
      run-query))
