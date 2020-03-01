(ns app.operations
  (:require [honeysql.core :as hsql]
            [clojure.string :as str]
            [app.dbcore :refer [run-query]]))

(defn patient-search-query [params]
  (let [p (map
           #(str % "%")
           (-> params str/trim (str/split #" ")))]
    (hsql/format {:select [:*]
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
                             [:like (hsql/raw "resource#>>'{name, 0, given, 0}'") (first p)]]])})))

(defn patient-search [req]
  (run-query (patient-search-query req)))

(defn patient-by-id-query [params]
  (hsql/format {:select [:resource]
                :from [:patient]
                :where [:= :patient.id params]}))

(defn patient-by-id [req]
  (run-query (patient-by-id-query req)))
