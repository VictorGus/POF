(ns app.migrations
  (:require [app.dbcore :refer [run-query]]
            [honeysql.core :as hsql]
            [app.fhirbase-ops :as fb]
            [app.manifest :as m])
  (:import java.util.Base64))

(defn create-users []
  (let [users (get m/manifest :users)]
    (doseq [user users]
      (when (empty? (-> {:select [:resource]
                         :from [:public_user]
                         :where [:= (hsql/raw "resource->>'name'")
                                 (:name user)]}
                        hsql/format
                        run-query))
        (fb/fhirbase-create (merge user {:resourceType "public_user"}))))))
