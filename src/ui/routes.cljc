(ns ui.routes)

(def routes {:. :patients/index
             "patients" {:. :patients/index
                         "create" {:. :ui.patient-workflow.card.model/create}}
             "patient" {[:uid] {:. :ui.patient-workflow.card.model/index-card
                                "edit" {:. :ui.patient-workflow.card.model/edit}}}
             "audit"   {"logs" {:. :ui.audit.logs.model/logs}}})

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
