(ns app.p-log.search
  (:require [clj-http.client :as http]
            [honeysql.core :as hsql]
            [chrono.core :as ch]
            [app.manifest :as m]
            [cheshire.core :as json]))

(defn time-range [{:keys [gte lte]}]
  (let [iso-fmt [:year "-" :month "-" :day "T"]
        gte (when gte
              (ch/format (ch/- (select-keys (ch/parse gte) iso-fmt) {:day 1}) iso-fmt))
        lte (when lte
              (ch/format (ch/+ (select-keys (ch/parse lte) iso-fmt) {:day 1}) iso-fmt))]
    {:range {:ts {:format "strict_date_optional_time"
                  :gte (or gte "now-1d/d")
                  :lte (or lte "now")}}}))

(defn query-string [{:keys [search]}]
  (when search
    {:query_string {:query search
                    :allow_leading_wildcard false
                    :default_operator "AND"}}))

(defn method [{:keys [action]}]
  {:term {:l_m action}})

(defn mk-es-request [params]
  (let [tr (time-range params)
        qs (query-string params)
        act (method params)]
    {:bool
     {:must [tr qs act]}}))

(defn logs-search [{{{:keys [params]} :body params :params headers :headers} :request}]
  (if-let [host (get-in m/manifest [:config :elastic :host])]
    (let [query (mk-es-request params)
          size  (or (:size params) 100)
          from  (or (:from params) 0)
          {:keys [status headers body error] :as resp}
          (http/request
           {:url            (str host "/_search")
            :accept         :json
            :as             :text
            :request-method :post
            :content-type   :json
            :body           (json/generate-string
                             {:size size
                              :from from
                              :query query
                              :sort [{:ts {:order "desc" :unmapped_type "long"}}]})})]
      (if-not error
        (update resp :body #(json/generate-string (-> (json/parse-string % true)
                                                      (assoc :search-query query))))
        resp))
    {:status 404 :body   {:message "Audit tools is not installed."}}))
