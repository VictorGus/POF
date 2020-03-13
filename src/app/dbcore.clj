(ns app.dbcore
  (:require [hikari-cp.core :as hc]
            [honeysql.core :as hsql]
            [honeysql.types :as hsqltypes]
            [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :refer :all]))

(def pool-configuration (delay (pg/pool :host (or (System/getenv "PGHOST") "localhost")
                                        :port (or (System/getenv "PGPORT") 5443)
                                        :user (or (System/getenv "PGUSER") "postgres")
                                        :password (or (System/getenv "PGPASSWORD") "postgres")
                                        :dbname (or (System/getenv "PGDATABASE") "fhirbase")
                                        :hikari {:read-only true})))
(defn run-query [query]
  (jdbc/query @pool-configuration query))
