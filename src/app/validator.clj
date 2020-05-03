(ns app.validator
  (:require [json-schema.core :as validator]
            [clojure.java.io :as io]
            [app.manifest :as m]
            [cheshire.core :as json]))

(defn compile-schema []
  (let [schema    (-> m/manifest
                      (get-in [:config :json-schema :schema])
                      io/resource
                      slurp
                      (json/parse-string true))
        required (map name (get-in m/manifest [:config :json-schema :required]))]
    (validator/compile (update-in schema [:definitions :Patient :required] concat required))))

(defn validate-resource [resource]
  (let [validator (compile-schema)]
    (validator resource)))
