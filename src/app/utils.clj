(ns app.utils)

(defn remove-nils [nm]
  (clojure.walk/postwalk
   (fn [el]
     (if (map? el)
       (not-empty (into {} (remove (comp nil? second)) el))
       el))
   nm))

(defn vec-search [value coll]
  (first (filter (fn [el]
                   (let [target? (cond (map? el)
                                       (first (filter (fn [el-inner]
                                                        (if (or (map? el-inner) (vector? el-inner))
                                                          (vec-search value el-inner)
                                                          (= el-inner value))) (vals el)))
                                       (vector? el)
                                       (vec-search value el)
                                       :else
                                       (= el value))]
                     target?))
                 (cond-> coll (map? coll) vals))))

(vec-search 3 [{:a 2 :b 4} {:a 1 :b 6}])

(defn deep-merge [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (if (some identity vs)
      (reduce #(rec-merge %1 %2) v vs)
      v)))

(defn wrap-apostrophes [s]
  (str "'" s "'"))
