(ns ui.patient-workflow.card.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.pages :as pages]
            [ui.styles :as styles]
            [ui.helper :as helper]
            [clojure.string :as str]
            [ui.zframes.redirect :as redirect]
            [ui.zframes.flash :as flash]
            [ui.basic-components.info-input :refer [info-input]]
            [ui.basic-components.form.view :as basic-form]
            [ui.patient-workflow.card.form :as form]
            [ui.patient-workflow.card.model :as model]))

(def card-style
  (styles/style
   [:#patient-card-wrapper
    {:margin-bottom "5px"}
    [:#patient-card
     {:border-radius "8px"
      :padding-top "15px"
      :border "1px solid rgba(51, 51, 51, 0.1)"}
     [:.patient-name
      {:font-size "24px"
       :margin "0px"
       :font-weight "900"}]
     [:.info-item
      [:#label-item
       {:height "23px"
        :font-size "15px"}]]
     [:.patient-info
      {:display "flex"
       :margin-bottom "5px"}
      [:.patient-info-item
       {:border-radius "8px"
        :margin-right "5px"
        :border "1px solid rgba(51, 51, 51, 0.1)"}]
      [:.card-title {:font-weight "700"
                     :font-size "20px"}]
      [:.card-title-inner {:font-weight "600"
                           :margin-bottom "0px"
                           :font-size "16px"}]]
     [:.patient-title-wrapper {:display "flex" :align-items "center"}
      [:.edit-button  {:position "absolute" :right 0 :top 0 :cursor "pointer"}]]
     [:.icon
      {:height "65px"
       :width "65px"
       :margin-top "10px"
       :padding-right "10px"}
      [:.icon.img
       {:fill "blue"}]]]]
   [:.remove-icon {:display "none"}]
   [:.form-row:hover [:.remove-icon {:display "block"
                                     :position "absolute"
                                     :z-index "10"
                                     :cursor "pointer"
                                     :right "16px"
                                     :width "15px"
                                     :height "15px"}]]
   [:.info-item {:margin-right "10px"}]
   [:.info-header {:font-size "22px"
                   :font-weight "900"
                   :color "white"
                   :background-color "#0069d9"}]
   [:.selector {:cursor "pointer"}]
   [:.flashes {:position "fixed" :top "20px" :right "20px" :max-width "500px" :z-index 200}
    [:ul {:padding-left "20px"}]]))

(defn encounter [item]
  [:div.card-body
   [:h5.card-title {:style {:float "left"}}
    (:e_type item)]
   [:p.text-muted.pl-2 {:style {:text-align "right"}}
    (:period_end item)]
   [:p.text-muted (some-> (:code item) clojure.string/capitalize)]
   [:span.info-item
    [:span.text-muted "Reason: "]
    (or (:reason item) "Not defined")]
   [:span.info-item
    [:span.text-muted "Status: "]
    (some-> (:status item) clojure.string/capitalize)]])

(defn patient-card [data]
  (fn [data]
    [:div#patient-card-wrapper card-style
     [:div.row
      [:div#patient-card.col-md-6.offset-md-3
       [:div.patient-title-wrapper
        [:div.icon
         [:img {:src (if (= "male" (:gender (first (:patient data))))
                       "male.svg"
                       "female.svg")}]]
        [:div
         [:p.patient-name (let [name (:patient_name (first (:patient data)))]
                            (str (first (:given name)) " " (:family name)))]
         [:p {:class "text-muted"
              :style {:margin-bottom "0px"}} (:birthdate (first (:patient data)))]]
        [:p.edit-button.mt-2.mr-2 [:i.fas.fa-edit {:style {:color "#0069d9"}
                                                   :on-click #(rf/dispatch [::redirect/redirect
                                                                            {:uri (helper/make-href (.-href (.-location js/window)) "edit")}])}]]]
       [:br]
       [:div.card
        [:div.card-header.info-header "Administrative info"]
        (when (:telecom (first (:patient data)))
          [:div.card-body
           [:h5.card-title "Telecom"]
           (for [item (:telecom (first (:patient data)))]
             [:<>
              [:span.info-item
               [:span.text-muted "Use: "]
               (:use item)]
              [:span.info-item
               [:span.text-muted "Type: "]
               (:system item)]
              [:span.info-item
               [:span.text-muted "Phone nubmer: "]
               (:value item)]])])
        (when (:address (first (:patient data)))
          [:div.card-body
           [:h5.card-title "Address"]
           (for [item (:address (first (:patient data)))]
             [:<>
              [:span.info-item
               [:span.text-muted "Country: "]
               (:country item)]
              [:span.info-item
               [:span.text-muted "City: "]
               (:city item)]
              [:span.info-item
               [:span.text-muted "Postal code: "]
               (:postalCode item)]
              [:span.info-item
               [:span.text-muted "State: "]
               (:state item)]
              [:div.info-item
               [:span.text-muted "Line: "]
               (first (:line item))]])])
        (when (:identifier (first (:patient data)))
          [:div.card-body
           [:h5.card-title "Identifiers"]
           [:span.info-item
            [:span.text-muted "SSN: "]
            (:value (helper/vec-search "SB" (:identifier (first (:patient data)))))]
           [:span.info-item
            [:span.text-muted "MRN: "]
            (:value (helper/vec-search "MR" (:identifier (first (:patient data)))))]
           [:span.info-item
            [:span.text-muted "Driver Licence: "]
            (:value (helper/vec-search "DL" (:identifier (first (:patient data)))))]])]
       [:br]
       [:div.card
        [:div.card-header.info-header "Recent encounters"]
        (for [item (:encounter data)]
          [encounter item])]]]]))

(pages/reg-subs-page
 model/index-card
 (fn [{:keys [data] :as page} params]
   [:div
    [:nav {:aria-label "breadcrumb"}
     [:ol.breadcrumb
      [:li.breadcrumb-item
       [:a {:href "#"} "Patients"]]
      [:li.breadcrumb-item.active
       (:uid params)]]]
    [patient-card data]
    [flash/flashes]]))

(defn patient-edit-workflow [data]
  [:div#patient-card-wrapper card-style
   [:div.row
    [:div#patient-card.col-md-6.offset-md-3
     [:div.card
      [:div.card-header.info-header "Patient personal info"]
      [:div.patient-title-wrapper
       [:form
        [:div.form-group.p-3
         (let [name (get-in data [:patient 0 :patient_name])]
           [:div.row.mb-3
            [:div.col-sm
             [:label.text-muted {:for "family-input"} "Family"]
             [basic-form/form-input [form/form-path :name 0 :family]
              "Enter family name" (:family name)]]
            [:div.col-sm
             [:label.text-muted {:for "Given-input"} "Given"]
             [basic-form/form-input [form/form-path :name 0 :given 0]
              "Enter given name" (get-in name [:given 0])]]
            (when-let [middle (get-in name [:given 1])]
              [:div.col-sm
               [:label.text-muted {:for "Middle-input"} "Middle"]
               [basic-form/form-input [form/form-path :name 0 :given 1]
                "Enter middle name" middle]])
            [:div.col-sm
             [:label.text-muted {:for "bd-input"} "Birth date"]
             [basic-form/form-input [form/form-path :birthDate]
              "Enter birth date" (:birthdate (first (:patient data)))]]])
         [:div.row
          [:div.col-sm-8
           [:label.text-muted {:for "gender-input"} "Gender"]
           [basic-form/form-select [{:value "male"    :display "Male"}
                                    {:value "female"  :display "Female"}
                                    {:value "other"   :display "Other"}
                                    {:value "unknown" :display "Unknown"}] [form/form-path :gender] (:gender (first (:patient data)))]]]]]]]
     [:br]
     [:div.card
      [:div.card-header.info-header "Administrative info"]
      (when (:telecom (first (:patient data)))
        (let [counter (atom -1)]
          [:div.card-body.border-bottom
           [:h5.card-title "Telecom"]
           (for [item (:telecom (first (:patient data)))]
             (do
               (swap! counter inc)
               [:div.row.form-row
                [:div.col-sm.mb-2
                 [:label.text-muted {:for "use-input"} "Use"]
                 [basic-form/form-select [{:value "work" :display "Work"}
                                          {:value "home" :display "Home"}
                                          {:value "mobile" :display "Mobile"}
                                          {:value "temp" :display "Temp"}
                                          {:value "old" :display "Old"}] [form/form-path :telecom @counter :use] (:use item)]]
                [:div.col-sm
                 [:label.text-muted {:for "system-input"} "Type"]
                 [basic-form/form-select [{:value "phone" :display "Phone"}
                                          {:value "fax" :display "Fax"}
                                          {:value "email" :display "Email"}
                                          {:value "url" :display "URL"}
                                          {:value "pager" :display "Pager"}
                                          {:value "sms" :display "SMS"}
                                          {:value "other" :display "Other"}] [form/form-path :telecom @counter :system] (:system item)]]
                [:div.col-sm
                 [:label.text-muted {:for "value-input"} (cond
                                                           (#{"phone" "fax" "sms" "pager"} (:system item))
                                                           "Phone number"
                                                           (#{"url"} (:system item))
                                                           "Contact url"
                                                           (#{"email"} (:system item))
                                                           "Email"
                                                           :else
                                                           "Telecom value")]
                 [basic-form/form-input [form/form-path :telecom @counter :value]
                  "Enter telecom value" (:value  item)]]
                [:i.fas.fa-trash-alt.remove-icon
                 {:on-click #(rf/dispatch [::model/remove-item [:telecom @counter]])}]]))
           [:button.btn.btn-link.mt-2
            {:on-click #(do
                          (rf/dispatch [::model/add-item :telecom]))}
            "+ Add telecom"]]))
      (when (:address (first (:patient data)))
        (let [counter (r/atom -1)]
          [:div.card-body.border-bottom
           [:h5.card-title "Address"]
           (for [item (:address (first (:patient data)))]
             (do
               (swap! counter inc)
               [:div
                [:div.row.mb-3.form-row
                 [:div.col
                  [:label.text-muted {:for "country-input"} "Country"]
                  [basic-form/form-input [form/form-path :address @counter :country]
                   "Enter country" (:country item)]]
                 [:div.col
                  [:label.text-muted {:for "city-input"} "City"]
                  [basic-form/form-input [form/form-path :address @counter :city]
                   "Enter city" (:city item)]]
                 [:div.col
                  [:label.text-muted {:for "postal-input"} "Postal code"]
                  [basic-form/form-input [form/form-path :address @counter :postalCode]
                   "Enter postal code" (:postalCode  item)]]
                 [:div.col
                  [:label.text-muted {:for "state-input"} "State"]
                  [basic-form/form-input [form/form-path :address @counter :state]
                   "Enter state" (:state  item)]]
                 [:i.fas.fa-trash-alt.remove-icon
                  {:on-click #(rf/dispatch [::model/remove-item [:address @counter]])}]]
                [:div.row
                 [:div.col-sm-6
                  [:label.text-muted {:for "state-input"} "Line"]
                  [basic-form/form-input [::form/form-path :address @counter :line]
                   "Enter line" (:line item)]]]
                [:br]]))
           [:button.btn.btn-link.mt-2
            {:on-click #(do
                          (swap! counter inc)
                          (rf/dispatch [::model/add-item :address]))}
            "+ Add address"]]))
      (when (:identifier (first (:patient data)))
        [:div.card-body
         [:h5.card-title "Identifiers"]
         [:div
          [:div.row.mb-3
           [:div.col
            [:label.text-muted {:for "ssn-input"} "Social security number"]
            [basic-form/form-input [form/form-path :identifier :SB]
             "Enter SSN"
             (:value  (helper/vec-search "SB" (:identifier (first (:patient data)))))]]
           [:div.col
            [:label.text-muted {:for "dl-input"} "Driver license"]
            [basic-form/form-input [form/form-path :identifier :DL]
             "Enter DL"
             (:value (helper/vec-search "DL" (:identifier (first (:patient data)))))]]]
          [:div.row
           [:div.col-sm-6
            [:label.text-muted {:for "mrn-input"} "Medical record number"]
            [basic-form/form-input [form/form-path :identifier :MR]
             "Enter MRN"
             (:value (helper/vec-search "MR" (:identifier (first (:patient data)))))]]]]])]
     [:button.btn.btn-outline-primary.mt-3.mb-2.mr-2
      {:on-click #(do
                    (rf/dispatch [::model/apply-changes])
                    (js/setTimeout (fn []
                                     (rf/dispatch [::redirect/redirect
                                                   {:uri (helper/make-back-href (.-href (.-location js/window)))}])) 1000))}
      "Save"]
     [:button.btn.btn-outline-danger.mt-3.mb-2
      {:on-click #(rf/dispatch [::redirect/redirect
                                {:uri (helper/make-back-href (.-href (.-location js/window)))}])}
      "Cancel"]]]])

(pages/reg-subs-page
 model/edit
 (fn [{:keys [data] :as page} params]
   [:div
    [:nav {:aria-label "breadcrumb"}
     [:ol.breadcrumb
      [:li.breadcrumb-item
       [:a {:href "#"} "Patients"]]
      [:li.breadcrumb-item
       [:a {:href (str "#" (helper/make-back-href (.-href (.-location js/window))))}
        (:uid params)]]
      [:li.breadcrumb-item.active
       "edit"]]]
    [patient-edit-workflow data]]))

