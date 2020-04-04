(ns ui.helpers)

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
