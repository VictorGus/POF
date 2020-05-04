(ns app.crud-test
  (:require [app.crud :as sut]
            [app.dbcore :as db]
            [matcho.core :as matcho]
            [clojure.walk :as walk]
            [clojure.test :refer :all]
            [clojure.walk :as walk]))

(use-fixtures :each
  (fn [f]
    (db/truncate-test "Patient")
    (f)
    (db/truncate-test "Patient")))

(deftest crud-create
  (testing "Patient crud create test"
    (with-redefs [db/run-query db/run-test-query]
      (sut/r-create {:body {:name {:given ["Test"] :family "Test"}}} "pt-1" true)
      (matcho/match
       {:resource {:name {:given ["Test"]
                          :family "Test"}
                   :resourceType "Patient"}}
       (-> (db/run-query "select resource from patient where id = 'pt-1'")
           first
           walk/keywordize-keys)))))

(deftest crud-delete
  (testing "Patient crud create test"
    (with-redefs [db/run-query db/run-test-query]
      (sut/r-create {:body {:name {:given ["Test"] :family "Test"}}} "pt-1" true)
      (sut/r-delete "pt-1")
      (matcho/match
       nil
       (-> (db/run-query "select resource from patient where id = 'pt-1'")
           first
           walk/keywordize-keys)))))

(deftest crud-read
  (testing "Patient crud create test"
    (with-redefs [db/run-query db/run-test-query]
      (sut/r-create {:body {:name {:given ["Test"] :family "Test"}}} "pt-1" true)
      (matcho/match
       {:resource {:name {:given ["Test"]
                          :family "Test"}
                   :resourceType "Patient"}}
       (-> (sut/r-read "pt-1")
           (get-in [:body :entry])
           first
           walk/keywordize-keys)))))

(deftest crud-update
  (testing "Patient crud create test"
    (with-redefs [db/run-query db/run-test-query]
      (sut/r-create {:body {:name {:given ["Test"] :family "Test"} :resourceType "Patient"}} "pt-1" true)
      (sut/r-update {:body {:name {:given ["Test1"] :family "Test1"} :resourceType "Patient" :id "pt-1"}} true)
      (matcho/match
       {:resource {:name {:given ["Test1"]
                          :family "Test1"}
                   :resourceType "Patient"}}
       (-> (sut/r-read "pt-1")
           (get-in [:body :entry])
           first
           walk/keywordize-keys)))))
