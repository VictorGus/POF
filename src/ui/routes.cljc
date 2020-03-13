(ns ui.routes)

(def routes {:. :ui.patient-workflow.model/index
             "patients" {:. :ui.patient-workflow.model/index}
             "patient" {[:uid] {:. :ui.patient-workflow.card.model/index-card}}})

(defn route-index* [route pth]
  (merge
   (hash-map (str (:. route))
             (-> route
                 (select-keys [:breadcrumb :audit-title])
                 (assoc :pth pth)))
   (reduce-kv
    (fn [acc k v]
      (if (or (string? k) (vector? k) )
        (merge acc (route-index* v (into pth [k "/" ])))
        acc))
    {}
    route)))

(def route-index
  (route-index* routes ["/"]))
