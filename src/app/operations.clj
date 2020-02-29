(ns app.operations
  (:require [honeysql.core :as hsql]
            [clojure.string :as str]
            [app.dbcore :refer [run-query]]))

(defmulti exec-operation (fn [operation _ _] operation))

(defn patient-search [params]
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

(defn patient-by-id [params]
  (hsql/format {:select [:resource]
                :from [:patient]
                :where [:= :patient.id params]}))

(defmethod exec-operation
  :patient-search
  [_ t params]
  (run-query (patient-search params)))

(defmethod exec-operation
  :patient-by-id
  [_ t params]
  (run-query (patient-by-id params)))
