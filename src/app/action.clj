(ns app.action
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
        (fb/fhirbase-create (merge user {:resourceType "public_user"
                                         :password (String. (.decode (Base64/getDecoder) (:password user)))}))))))

(defn log-in [{{:keys [login password]} :params :as req}]
  (cond
    (and name password)
    (let [matched-users (-> {:select [:resource]
                             :from [:public_user]
                             :where [:and
                                     [:= (hsql/raw "resource->>'name'") login]
                                     [:= (hsql/raw "resource->>'password'") (String. (.decode (Base64/getDecoder) password))]]}
                            hsql/format
                            run-query)]
      (if (not-empty matched-users)
        {:status 200
         :body {:message "Ok"}}
        {:status 401
         :body {:message "Access denied"}}))
    :else
    {:status 401
     :body {:message "Access denied"}}))
