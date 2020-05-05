(ns ui.audit.logs.model
  (:require [re-frame.core :as rf]
            [ui.audit.logs.form :as form]
            [chrono.core :as ch]))

(def almost-iso-fmt [:year "-" :month "-" :day " " :hour ":" :min ":" :sec])

(def logs ::logs)

(defn humanize-action [req]
  (let [action-map {"get"    "Viewing "
                    "post"   "Creating "
                    "delete" "Deleting "
                    "patch"  "Updating "
                    "put"    "Updating "}]
    (cond-> (assoc req :l_m (get action-map (:l_m req)))
      (= "/Patient/search" (:l_uri req))
      (assoc :l_m "Search "))))

(defn humanize-ts [req]
  (update req :ts #(-> % ch/parse (ch/+ {:hour 3}) (ch/format almost-iso-fmt))))

(rf/reg-event-fx
 ::send-req
 (fn [{db :db} _]
   (let [form-values (get db form/form-path)]
     {:dispatch [::send-data form-values]})))

(rf/reg-event-fx
 ::send-data
 (fn [{db :db} [_ form-values]]
   {:xhr/fetch {:uri "/Logs/"
                :params (into {} (filter second form-values))
                :req-id logs
                :success {:event ::save-results}}}))

(rf/reg-event-fx
 ::save-results
 (fn [{db :db} [_ data]]
   {:db (assoc db logs data)}))

(rf/reg-event-fx
 logs
 (fn [{db :db} [pid phase params]]
   (cond
     (= :deinit phase)
     {}
     (or (= :params phase) (= :init phase))
     {:xhr/fetch {:uri "/Logs/"
                  :req-id logs}})))

(rf/reg-sub
 logs
 :<- [:pages/data   logs]
 :<- [:xhr/response logs]
 (fn [[page resp] [pid]]
   (let [sanitized-logs (map
                         (comp humanize-action humanize-ts :_source)
                         (get-in resp [:data :hits :hits]))]
     (merge page {:data sanitized-logs}))))
