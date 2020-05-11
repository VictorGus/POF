(ns app.action
  (:require [app.dbcore :refer [run-query-first run-query]]
            [honeysql.core :as hsql]
            [clj-jwt.core :refer :all]
            [clj-jwt.key  :refer [private-key public-key]]
            [clj-time.core :refer [now plus days minutes]]
            [cheshire.core :as json]
            [app.fhirbase-ops :as fb]
            [app.manifest :as m]
            [clojure.walk :as walk]
            [clojure.string :as str])
  (:import java.util.Base64
           java.security.MessageDigest))

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

(defn create-clients []
  (let [clients (get m/manifest :clients)]
    (doseq [client clients]
      (when (empty? (-> {:select [:resource]
                         :from [:client]
                         :where [:= (hsql/raw "resource->>'name'")
                                 (:name client)]}
                        hsql/format
                        run-query))
        (fb/fhirbase-create (merge client {:resourceType "client"}))))))

(defn sha256 [string]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.getBytes string "UTF-8"))]
    (apply str (map (partial format "%02x") digest))))

(def secret (sha256 "cHV0aW4tdm9y"))

(defn verify-token [token]
  (try
    (some-> token str->jwt (verify secret))
    (catch Exception e false)))

(defn log-in [{body :body :as req}]
  (let [body (if (map? body)
               body
               (slurp body))
        {:keys [login password]} (cond-> body
                                   (string? body)
                                   (json/parse-string true))]
    (cond
      (and login password)
      (let [{:keys [id resource] :as matched-user} (-> {:select [:resource :id]
                                                        :from [:public_user]
                                                        :where [:and
                                                                [:= (hsql/raw "resource->>'name'") login]
                                                                [:= (hsql/raw "resource->>'password'") (String. (.decode (Base64/getDecoder) password))]]}
                                                       hsql/format
                                                       run-query-first)
            claim  {:iss "cHV0aW4tdm9y" :sub id :exp (plus (now) (days 1)) :iat (now)}
            token     (-> claim jwt (sign :HS256 secret) to-str)]
        (if matched-user
          {:status 200
           :body {:message "Ok"
                  :jwt token}}
          {:status 401
           :body {:message "Access denied"}}))
      :else
      {:status 401
       :body {:message "Access denied"}})))

(defn decode-jwt [token]
  (-> token str->jwt :claims))

(defn basic-auth [cred]
  (let [[login password] (str/split (String. (.decode (Base64/getDecoder) cred)) #":")]
    (println "GOVNAEDI")
    (-> {:select [:*]
         :from [:client]
         :where [:and
                 [:= (hsql/raw "resource->>'name'") login]
                 [:= (hsql/raw "resource->>'password'") password]]}
        hsql/format
        run-query-first)))

(defn get-users [req]
  {:status 200
   :body (json/generate-string (-> {:select [:id :resource] :from [:public_user]} hsql/format run-query))})
