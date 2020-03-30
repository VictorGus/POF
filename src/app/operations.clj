(ns app.operations
  (:require [honeysql.core :as hsql]
            [clojure.string :as str]
            [cheshire.core :refer [generate-string]]
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

(defn patient-by-id-query [{{params :params} :params}]
  (hsql/format {:select [(hsql/raw "p.resource#>>'{address}' as address")
                         (hsql/raw "p.resource#>>'{telecom}' as telecom")
                         (hsql/raw "p.resource#>>'{identifier}' as identifier")
                         (hsql/raw "p.resource#>>'{name, 0}' as patient_name")
                         (hsql/raw "e.resource#>>'{class, code}' as code")
                         (hsql/raw "e.resource#>>'{period, endfd fs}' as period_end")
                         (hsql/raw "e.resource#>>'{type, 0, text}' as e_type")
                         (hsql/raw "e.resource#>>'{reason, 0, coding, 0, display}' as reason")]
                :from [[:patient :p]]
                :join [[:encounter :e] [:=
                                        (hsql/raw "e.resource #>> '{subject, id}'")
                                        :p.id]]
                :where [:= :p.id params]
                :order-by [[(hsql/raw "e.resource#>>'{period, end}'") :desc]]
                :limit 3}))

(defn patient-by-id [req]
  {:status 200
   :body (run-query (patient-by-id-query req))})
