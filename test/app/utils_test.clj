(ns app.utils-test
  (:require [app.utils :as sut]
            [matcho.core :as matcho]
            [clojure.test :refer :all]))

(deftest backend-utils
  (testing "Function remove-nils"
    (matcho/match nil (sut/remove-nils {:a nil :b nil}))
    (matcho/match nil (sut/remove-nils nil))
    (matcho/match {:a 2 :b {:d 2}} (sut/remove-nils {:a 2 :b {:c nil :d 2}}))
    (matcho/match {:a [nil {:b 2}] :b 2} (sut/remove-nils {:a [{:a nil :b nil} {:b 2}] :b 2})))

  (testing "Function vec-search"
    (matcho/match nil (sut/vec-search 3 [{:a 2 :b 4} {:a 1 :b 6}]))
    (matcho/match {:a 2 :b {:c 3}} (sut/vec-search 3 [{:a 2 :b {:c 3}} {:a 1 :b 6}]))
    (matcho/match {:a 2 :b 3}      (sut/vec-search 3 [{:a 2 :b 3} {:a 1 :b 6}])))

  (testing "Function deep-merge"
    (matcho/match []  (sut/deep-merge {:a 2} []))
    (matcho/match nil (sut/deep-merge nil nil))
    (matcho/match {:a 2} (sut/deep-merge {:a 2} nil))
    (matcho/match {:a 2 :b {:c 3 :d 4}} (sut/deep-merge {:a 2 :b {:c 3}} {:a 2 :b {:c 3 :d 4}}))
    (matcho/match {:a [{:c 6 :d 3}]} (sut/deep-merge {:a [{:c 5 :d 3}]} {:a [{:c 6}]}))))

