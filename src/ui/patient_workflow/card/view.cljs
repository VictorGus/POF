(ns ui.patient-workflow.card.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.pages :as pages]
            [ui.styles :as styles]
            [ui.helper :as helper]
            [clojure.string :as str]
            [ui.zframes.redirect :as redirect]
            [ui.basic-components.info-input :refer [info-input]]
            [ui.patient-workflow.card.model :as model]))

(def card-style
  (styles/style
   [:#patient-card-wrapper
    [:#patient-card
     {:border-radius "8px"
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
   [:.info-item {:margin-right "10px"}]
   [:.info-header {:font-size "22px"
                   :font-weight "900"
                   :color "white"
                   :background-color "#0069d9"}]
   [:.selector {:cursor "pointer"}]))

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
      [:div#patient-card {:class "col-md-6 offset-md-3"}
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
             [:<> [:span.info-item
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
       "8e369267-1e8a-438f-8ae8-a100fe2103fe"]]]
    [patient-card data]]))

(defn patient-edit-workflow [data]
  [:div#patient-card-wrapper card-style
     [:div.row
      [:div#patient-card {:class "col-md-6 offset-md-3"}
       [:div.card
        [:div.card-header.info-header "Patient personal info"]
        [:div.patient-title-wrapper
         [:form
          [:div.form-group.p-3
           (let [name  (get-in data [:patient 0 :patient_name])]
             [:div.row.mb-3
              [:div.col-sm
               [:label.text-muted {:for "family-input"} "Family"]
               [:input.form-control {:type "text"
                                     :id "family-input"
                                     :placeholder "Enter family name"
                                     :value (:family name)}]]
              [:div.col-sm
               [:label.text-muted {:for "Given-input"} "Given"]
               [:input.form-control {:type "text"
                                     :id "given-input"
                                     :placeholder "Enter given name"
                                     :value (get-in name [:given 0])}]]
              (when-let [middle (get-in name [:given 1])]
                [:div.col-sm
                 [:label.text-muted {:for "Middle-input"} "Middle"]
                 [:input.form-control {:type "text"
                                       :id "middle-input"
                                       :placeholder "Enter middle name"
                                       :value middle}]])
              [:div.col-sm
               [:label.text-muted {:for "bd-input"} "Birth date"]
               [:input.form-control {:type "text"
                                     :id "bd-input"
                                     :placeholder "Enter birth date"
                                     :value (:birthdate (first (:patient data)))}]]])
           [:div.row
            [:div.col-sm-8
             [:label.text-muted {:for "gender-input"} "Gender"]
             [:select.form-control.selector {:type "text"
                                             :id "gender-input"}
              [:option.selector-item (cond-> {:value "male"}
                                       (= "male" (:gender (first (:patient data))))
                                       (assoc :selected "selected")) "Male"]
              [:option.selector-item (cond-> {:value "female"}
                         (= "female" (:gender (first (:patient data))))
                         (assoc :selected "selected")) "Female"]
              [:option.selector-item (cond-> {:value "other"}
                         (= "other" (:gender (first (:patient data))))
                         (assoc :selected "selected")) "Other"]
              [:option.selector-item (cond-> {:value "unknown"}
                         (= "unknown" (:gender (first (:patient data))))
                         (assoc :selected "selected")) "Unknown"]]]]]]]]
       [:br]
       [:div.card
        [:div.card-header.info-header "Administrative info"]
        (when (:telecom (first (:patient data)))
          [:div.card-body.border-bottom
           [:h5.card-title "Telecom"]
           (for [item (:telecom (first (:patient data)))]
             [:div.row
              [:div.col-sm
               [:label.text-muted {:for "use-input"} "Use"]
               [:select.form-control.selector {:type "text"
                                               :id "use-input"}
                [:option.selector-item (cond-> {:value "home"}
                                         (= "home" (:use item))
                                         (assoc :selected "selected")) "Home"]
                [:option.selector-item (cond-> {:value "work"}
                                         (= "work" (:use item))
                                         (assoc :selected "selected")) "Work"]
                [:option.selector-item (cond-> {:value "mobile"}
                                         (= "mobile" (:use item))
                                         (assoc :selected "selected")) "Mobile"]
                [:option.selector-item (cond-> {:use "temp"}
                                         (= "temp" (:use item))
                                         (assoc :selected "selected")) "Temp"]
                [:option.selector-item (cond-> {:use "old"}
                                         (= "old" (:use item))
                                         (assoc :selected "selected")) "Old"]]]
              [:div.col-sm
               [:label.text-muted {:for "system-input"} "Type"]
               [:select.form-control.selector {:type "text"
                                               :id "system-input"}
                [:option.selector-item (cond-> {:value "phone"}
                                         (= "phone" (:system item))
                                         (assoc :selected "selected")) "Phone"]
                [:option.selector-item (cond-> {:value "fax"}
                                         (= "fax" (:system item))
                                         (assoc :selected "selected")) "Fax"]
                [:option.selector-item (cond-> {:value "email"}
                                         (= "email" (:system item))
                                         (assoc :selected "selected")) "Email"]
                [:option.selector-item (cond-> {:value "url"}
                                         (= "url" (:system item))
                                         (assoc :selected "selected")) "URL"]
                [:option.selector-item (cond-> {:value "sms"}
                                         (= "sms" (:system item))
                                         (assoc :selected "selected")) "SMS"]
                [:option.selector-item (cond-> {:value "pager"}
                                         (= "pager" (:system item))
                                         (assoc :selected "selected")) "Pager"]
                [:option.selector-item (cond-> {:value "other"}
                                         (= "other" (:system item))
                                         (assoc :selected "selected")) "Other"]]]
              [:div.col-sm
               [:label.text-muted {:for "value-input"} (cond
                                                         (#{"phone" "fax" "sms" "pager"} (:system item))
                                                         "Phone number"
                                                         (#{"url"} (:system item))
                                                         "Contact url"
                                                         (#{"email"} (:system item))
                                                         "Email"
                                                         (#{"other"} (:system item))
                                                         "Telecom value")]
               [:input.form-control {:type "text"
                                     :id "value-input"
                                     :placeholder "Enter telecom value"
                                     :value (:value item)}]]])])
        (when (:address (first (:patient data)))
          [:div.card-body.border-bottom
           [:h5.card-title "Address"]
           (for [item (:address (first (:patient data)))]
             [:div
              [:div.row.mb-3
               [:div.col
                [:label.text-muted {:for "country-input"} "Country"]
                [:input.form-control {:type "text"
                                      :id "country-input"
                                      :placeholder "Enter country"
                                      :value (:country item)}]]
               [:div.col
                [:label.text-muted {:for "city-input"} "City"]
                [:input.form-control {:type "text"
                                      :id "city-input"
                                      :placeholder "Enter city"
                                      :value (:city item)}]]
               [:div.col
                [:label.text-muted {:for "postal-input"} "Postal code"]
                [:input.form-control {:type "text"
                                      :id "postal-input"
                                      :placeholder "Enter postal code"
                                      :value (:postalCode item)}]]
               [:div.col
                [:label.text-muted {:for "state-input"} "State"]
                [:input.form-control {:type "text"
                                      :id "state-input"
                                      :placeholder "Enter state"
                                      :value (:state item)}]]]
              [:div.row
               [:div.col-sm-6
                [:label.text-muted {:for "line-input"} "Line"]
                [:input.form-control {:type "text"
                                      :id "line-input"
                                      :placeholder "Enter line"
                                      :value (:line item)}]]]])])
        (when (:identifier (first (:patient data)))
          [:div.card-body
           [:h5.card-title "Identifiers"]
           [:div
            [:div.row.mb-3
             [:div.col
              [:label.text-muted {:for "ssn-input"} "Social security number"]
              [:input.form-control {:type "text"
                                    :id "ssn-input"
                                    :placeholder "Enter SSN"
                                    :value (:value (helper/vec-search "SB" (:identifier (first (:patient data)))))}]]
             [:div.col
              [:label.text-muted {:for "dl-input"} "Driver license"]
              [:input.form-control {:type "text"
                                    :id "dl-input"
                                    :placeholder "Enter DL"
                                    :value (:value (helper/vec-search "DL" (:identifier (first (:patient data)))))}]]]
            [:div.row
             [:div.col-sm-6
              [:label.text-muted {:for "mrn-input"} "Medical record number"]
              [:input.form-control {:type "text"
                                    :id "mrn-input"
                                    :placeholder "Enter MRN"
                                    :value (:value (helper/vec-search "MR" (:identifier (first (:patient data)))))}]]]]])]]]])

(pages/reg-subs-page
 model/edit
 (fn [{:keys [data] :as page} params]
   [patient-edit-workflow data]))

