(ns app.fhirbase-ops
  (:require [honeysql.core :as hsql]
            [cheshire.core :as json]
            [clojure.string :as str]
            [app.utils :as u]
            [app.validator :as validator]
            [app.dbcore :refer [run-query]]))

(defn fhirbase-create [resource & [skip-validation id]]
  (let [errors (validator/validate-resource resource)]
    (cond
      (and
       (not skip-validation)
       (not-empty (:errors errors)))
      errors
      :else
      (-> {:select [(hsql/call :fhirbase_create
                               (hsql/raw (u/wrap-apostrophes (str/replace (cond-> resource
                                                                            (and (map? resource) id)
                                                                            (-> (assoc :id id) json/generate-string)
                                                                            (map? resource)
                                                                            json/generate-string) #"'" ""))))]}
          hsql/format
          run-query
          first
          :fhirbase_create))))

(defn fhirbase-update [resource & [skip-validation id]]
  (let [errors (validator/validate-resource resource)]
    (cond
      (and
       (not skip-validation)
       (not-empty (:errors errors)))
      errors
      :else
      (-> {:select [(hsql/call :fhirbase_update
                               (hsql/raw (u/wrap-apostrophes (str/replace (cond-> resource
                                                                            (and (map? resource) id)
                                                                            (-> (assoc :id id) json/generate-string)
                                                                            (map? resource)
                                                                            json/generate-string) #"'" ""))))]}
          hsql/format
          run-query
          first
          :fhirbase_update))))

(defn fhirbase-delete [id]
  (-> {:select [(hsql/call :fhirbase_delete
                           "Patient" id)]}
      hsql/format
      run-query))
