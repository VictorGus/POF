(ns ui.audit.logs.model
  (:require [re-frame.core :as rf]
            [chrono.core :as ch]))

(def almost-iso-fmt [:year "-" :month "-" :day "T" :hour ":" :min ":" :sec])

(def logs ::logs)

(ch/format (ch/parse "2020-04-27T18:48:26.465Z") almost-iso-fmt)

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
