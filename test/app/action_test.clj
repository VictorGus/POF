(ns app.action-test
  (:require [app.action :as sut]
            [app.dbcore :as db]
            [app.fhirbase-ops :as fb]
            [matcho.core :as matcho]
            [clojure.test :refer :all]))

(use-fixtures :each
  (fn [f]
    (db/truncate-test "public_user")
    (f)
    (db/truncate-test "public_user")))

(deftest token-create-and-sign
  (testing "Verify JWT for authorized user"
    (with-redefs [db/run-query-first db/run-test-query-first
                  db/run-query db/run-test-query]
      (fb/fhirbase-create {:name "test"
                           :password "test"
                           :resourceType "public_user"} "u-1")
      (let [{{:keys [jwt]} :body} (sut/log-in {:body {:login "test"
                                                      :password "dGVzdA=="}})]
        (is true (sut/verify-token jwt))
        (matcho/match {:status 401
                       :body {:message "Access denied"}}
                       (sut/log-in {:body {:login "wrong"
                                           :password "dGVZdA=="}}))))))
