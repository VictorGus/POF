(ns app.operations
  (:require [honeysql.core :as hsql]
            [clojure.string :as str]
            [cheshire.core :as json]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [honeysql.helpers :refer :all]
            [honeysql-postgres.format :refer :all]
            [app.dbcore :refer [run-query]]))

(defn patient-search-query [params]
  (let [p (map
           #(str % "%")
           (-> params str/trim (str/split #" ")))]
    (hsql/format {:select [[(hsql/raw "json_object_agg(ids#>> '{type, coding, 0, code}', ids->> 'value')") :ids]
                           [(hsql/raw "p.resource #>> '{name, 0, given, 0}'") :given]
                           [(hsql/raw "p.resource #>> '{name, 0, family}'") :family]
                           [(hsql/raw "p.resource #>> '{address, 0, line, 0}'") :line]
                           [(hsql/raw "p.resource #>> '{address, 0, city}'") :city]
                           [(hsql/raw "p.resource #>> '{gender}'") :gender]
                           [(hsql/raw "p.resource #>> '{birthDate}'") :birthDate]
                           [(hsql/raw "p.resource #>> '{address, 0, country}'") :country]
                           [(hsql/raw "p.resource #>> '{address, 0, state}'") :st]
                           [(hsql/raw "p.resource #>> '{telecom, 0, value}'") :phone]
                           :p.id]
                  :from [[{:select [:*]
                           :from [:patient]
                           :where (cond
                                    (= (count p) 1)
                                    [:or
                                     [:ilike (hsql/raw "resource#>>'{name, 0, family}'") (first p)]
                                     [:ilike (hsql/raw "resource#>>'{name, 0, given, 0}'") (first p)]
                                     (hsql/raw (str "resource#>>'{identifier}' @@ 'value = " (first p) "'"))]
                                   (= (count p) 2)
                                    [:or
                                     [:and
                                      [:ilike (hsql/raw "resource#>>'{name, 0, family}'") (first p)]
                                      [:ilike (hsql/raw "resource#>>'{name, 0, given, 0}'") (second p)]]
                                     [:and
                                      [:ilike (hsql/raw "resource#>>'{name, 0, family }'") (second p)]
                                      [:ilike (hsql/raw "resource#>>'{name, 0, given, 0}'") (first p)]]])} :p]
                         [(hsql/raw "jsonb_array_elements(p.resource->'identifier')") :ids]]
                  :where [:or
                          (hsql/raw "ids #>> '{type, coding, 0, code}' = 'DL'")
                          (hsql/raw "ids #>> '{type, coding, 0, code}' = 'SB'")]
                  :group-by [:resource :id]})))

(defn patients-search [req]
  (let [normalized-req (str/replace (get-in req [:params :params]) #"%20" " ")]
    {:status 200
     :body (-> normalized-req
               patient-search-query
               run-query)}))

(defn patient-ehr-query [id]
  (let [patient-query (hsql/format {:select [(hsql/raw "p.resource#>'{address}' as address")
                                             (hsql/raw "p.resource#>'{telecom}' as telecom")
                                             (hsql/raw "p.resource#>'{gender}' as gender")
                                             (hsql/raw "p.resource#>'{birthDate}' as birthDate")
                                             (hsql/raw "p.resource#>'{identifier}' as identifier")
                                             (hsql/raw "p.resource#>'{name, 0}' as patient_name")]
                                    :from [[:patient :p]]
                                    :where [:= :p.id id]})
        encounter-query (hsql/format {:select [(hsql/raw "e.resource#>>'{reason, 0, coding, 0, display}' as reason")
                                               (hsql/raw "e.resource#>>'{class, code}' as code")
                                               (hsql/raw "e.resource#>>'{period, end}' as period_end")
                                               (hsql/raw "e.resource#>>'{status}' as status")
                                               (hsql/raw "e.resource#>>'{type, 0, text}' as e_type")]
                                      :from [[:encounter :e]]
                                      :where [:= (hsql/raw "resource#>>'{subject, id}'") id]
                                      :order-by [[(hsql/raw "resource#>>'{period, end}'") :desc]]
                                      :limit 3})]
    [patient-query encounter-query]))

(defn patient-ehr [{{params :params} :params}]
  (let [query (patient-ehr-query params)
        patient-info (run-query (first query))
        encounter-info (run-query (second query))]
    {:status 200
     :body {:patient patient-info
            :encounter encounter-info}}))

(defn patient-by-id [params]
  (let [id (if (map? params)
             (get-in params [:params :params])
             params)]
    {:status 200
     :body {:entry (run-query (hsql/format {:select [:resource]
                                            :from [:patient]
                                            :where [:= :id id]}))}}))

(defn patient-create [{{params :params} :params}]
  (let [query {:select (hsql/call :fhirbase_create (json/generate-string params))}]
    {:status 200
     :body "ok"}))

(defn deep-merge [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (if (some identity vs)
      (reduce #(rec-merge %1 %2) v vs)
      v)))


(defn patient-update [{body :body {params :params} :params :as request}]
  (let [body (walk/keywordize-keys (json/parse-string (slurp body)))
        resource (assoc (deep-merge (:resource (first (get-in (walk/keywordize-keys (patient-by-id params)) [:body :entry]))) body) :id params)
        query-result (run-query (hsql/format {:select [(hsql/call :fhirbase_create
                                                                  (hsql/raw (str "'" (str/replace (json/generate-string resource) #"'" "") "'")))]}))]
    {:status 200 :body query-result}))

