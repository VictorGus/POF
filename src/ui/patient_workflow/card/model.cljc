(ns ui.patient-workflow.card.model
  (:require [re-frame.core :as rf]
            [ui.helper :as helper]
            [ui.basic-components.form.model :as basic-form]
            [ui.zframes.flash :as flash]
            [ui.patient-workflow.card.form :as form]))

(def index-card ::index-card)
(def edit ::edit)
(def create ::create)

(rf/reg-event-fx
 index-card
 (fn [{db :db} [pid phase params]]
   {:xhr/fetch {:uri (str "/Patient/" (get-in db [:route-map/current-route :params :uid]) "/ehr")
                :req-id index-card}}))

(rf/reg-sub
 index-card
 :<- [:pages/data   index-card]
 :<- [:xhr/response index-card]
 (fn [[page {resp :data}] [pid]]
   (merge page {:data resp})))

(rf/reg-event-fx
 edit
 (fn [{db :db} [pid phase params]]
   (rf/dispatch [::form/init])
   {:xhr/fetch {:uri (str "/Patient/" (get-in db [:route-map/current-route :params :uid]) "/ehr")
                :req-id edit}}))

(rf/reg-event-fx
 create
 (fn [{db :db} _]
   {:db (-> db
            (assoc-in [create :create-items :address] [{}])
            (assoc-in [create :create-items :telecom] [{}]))}))

(rf/reg-sub
 create
 (fn [_]
   {}))

(rf/reg-sub
 ::create-items
 (fn [db _]
   (get-in db [create :create-items])))

(rf/reg-event-fx
 ::add-create-item
 (fn [{db :db} [_ path]]
   {:db (update-in db [create :create-items path] conj {})}))

(rf/reg-event-fx
 ::remove-create-item
 (fn [{db :db} [_ path]]
   {:db (update-in db [create :create-items (first path)] helper/vec-remove (second path))}))

(rf/reg-event-fx
 ::add-item
 (fn [{db :db} [_ path]]
   {:db (update-in db [:xhr :req edit :data :patient 0 path] conj {})}))

(rf/reg-event-fx
 ::remove-item
 (fn [{db :db} [_ path]]
   {:dispatch [::basic-form/remove-item (concat [form/form-path] path)]
    :db (update-in db [:xhr :req edit :data :patient 0 (first path)] helper/vec-remove (second path))}))

(rf/reg-event-fx
 ::apply-changes
 (fn [{db :db} _]
   {:dispatch-later [{:ms 0 :dispatch [::form/eval]}
                     {:ms 300   :dispatch [::send-data]}]}))

(defn normalize-identifiers [identifiers]
  (reduce-kv (fn [acc k v]
               (case k
                 :MR
                 (conj acc {:type {:text "Medical Record Number"
                                   :coding [{:code "MR"
                                             :system "http://hl7.org/fhir/v2/0203"
                                             :display "Medical Record Number"}]}
                            :value v
                            :system "http://hospital.smarthealthit.org"})
                 :SB
                 (conj acc {:type {:text "Social Security Number"
                                   :coding [{:code "SB"
                                             :system "http://hl7.org/fhir/identifier-type"
                                             :display "Social Security Number"}]}
                            :value v
                            :system "http://hl7.org/fhir/sid/us-ssn"})
                 :DL
                 (conj acc {:type {:text "Driver's License"
                                   :coding [{:code "DL"
                                             :system "http://hl7.org/fhir/v2/0203"
                                             :display "Driver's License"}]}
                            :value v
                            :system "urn:oid:2.16.840.1.113883.4.3.25"})))
             [] identifiers))

(rf/reg-event-fx
 ::send-data
 (fn [{db :db} _]
   (let [form-values (get db form/form-path)]
     (println (update-in form-values [:address 0 :line] vals))
     {:xhr/fetch {:uri    (str "/Patient/" (get-in db [:route-map/current-route :params :uid]))
                  :method "PUT"
                  :body (-> form-values
                            (update :identifier normalize-identifiers)
                            (update-in [:name 0 :given]   (comp vec vals))
                            (update-in [:address] (partial map #(update % :line (comp vec vals)))))}
      :dispatch-n [[:flash/success {:msg "Successfully saved"}]
                   [::form/init]]})))

(rf/reg-sub
 edit
 :<- [:pages/data   edit]
 :<- [:xhr/response edit]
 (fn [[page {resp :data}] [pid]]
   (merge page {:data resp})))
