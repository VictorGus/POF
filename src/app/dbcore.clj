(ns app.dbcore
  (:require [hikari-cp.core :as hc]
            [honeysql.core :as hsql]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :refer :all]))

(def pool-configuration {:pool-name "pof"
                         :adapter "postgresql"
                         :server-name (or (System/getenv "PGHOST") "localhost")
                         :port-number (or (System/getenv "PGPORT") 5443)
                         :username (or (System/getenv "PGUSER") "postgres")
                         :password (or (System/getenv "PGPASSWORD") "postgres")
                         :database-name (or (System/getenv "PGDATABASE") "fhirbase")})

(defonce datasource
  (delay (hc/make-datasource pool-configuration)))

(defn run-query [query]
  (jdbc/with-db-connection [conn {:datasource @datasource}]
    (jdbc/query conn query)))

