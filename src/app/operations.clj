(ns app.operations
  (:require [honeysql.core :as hsql]
            [clojure.string :as str]
            [cheshire.core :refer [generate-string]]
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
                                     [:like (hsql/raw "resource#>>'{name, 0, family}'") (first p)]
                                     [:like (hsql/raw "resource#>>'{name, 0, given, 0}'") (first p)]]
                                    (= (count p) 2)
                                    [:or
                                     [:and
                                      [:like (hsql/raw "resource#>>'{name, 0, family}'") (first p)]
                                      [:like (hsql/raw "resource#>>'{name, 0, given, 0}'") (second p)]]
                                     [:and
                                      [:like (hsql/raw "resource#>>'{name, 0, family }'") (second p)]
                                      [:like (hsql/raw "resource#>>'{name, 0, given, 0}'") (first p)]]])} :p]
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

(defn patient-by-id-query [params]
  (hsql/format {:select [:resource]
                :from [:patient]
                :where [:= :patient.id params]}))

(defn patient-by-id [req]
  (run-query (patient-by-id-query req)))
