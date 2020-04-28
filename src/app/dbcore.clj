(ns app.dbcore
  (:require [honeysql.core :as hsql]
            [honeysql.types :as hsqltypes]
            [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [app.manifest :as m]
            [honeysql.helpers :refer :all]))

(def pool-configuration (delay (pg/pool :host (get-in m/manifest [:config :db :host])
                                        :port (get-in m/manifest [:config :db :port])
                                        :user (get-in m/manifest [:config :db :user])
                                        :password (get-in m/manifest [:config :db :password])
                                        :dbname (get-in m/manifest [:config :db :dbname])
                                        :hikari {:read-only true})))

(defn run-query [query]
  (jdbc/query @pool-configuration query))

(defn run-query-first [query]
  (first (jdbc/query @pool-configuration query)))

(defn run-test-query [query]
  (jdbc/query (get-in m/test-app [:config :db]) query))

(defn truncate-test [t]
  (jdbc/execute! (get-in m/test-app [:config :db]) (str "truncate " t)))
