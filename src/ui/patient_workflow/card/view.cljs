(ns ui.patient-workflow.card.view
  (:require [reagent.core :as r]
            [ui.pages :as pages]
            [ui.styles :as styles]
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
     [:.patient-title-wrapper
      {:display "flex"
       :align-items "center"}]
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
                   :background-color "#0069d9"}]))
(defn encounter []
  [:div.card-body
   [:h5.card-title "Encounter for check-up"]
   [:p.text-muted "Ambulatory"]
   [:span.info-item
    [:span.text-muted "Reason: "]
    "Second degree burn"]
   [:span.info-item
    [:span.text-muted "Status: "]
    "Finished"]])

(defn patient-card []
  (let [data "TODO"]
    (fn []
      [:div#patient-card-wrapper card-style
       [:div.row
        [:div#patient-card {:class "col-md-6 offset-md-3"}
         [:div.patient-title-wrapper
          [:div.icon
            [:img {:src "male.svg"}]]
          [:div
           [:p.patient-name "Test Test"]
           [:p {:class "text-muted"
                :style {:margin-bottom "0px"}} "1955-02-13 (65 y.o.)"]]]
         [:br]
         [:div.card
          [:div.card-header.info-header "Administrative info"]
          [:div.card-body
           [:h5.card-title "Telecom"]
           [:span.info-item
            [:span.text-muted "Use: "]
            "Home"]
           [:span.info-item
            [:span.text-muted "Type: "]
            "Mobile"]
           [:span.info-item
            [:span.text-muted "Phone nubmer: "]
            "88005553555"]]
          [:div.card-body
           [:h5.card-title "Address"]
           [:span.info-item
            [:span.text-muted "Country: "]
            "USA"]
           [:span.info-item
            [:span.text-muted "City: "]
            "Brockton"]
           [:span.info-item
            [:span.text-muted "Postal code: "]
            "02301"]
           [:span.info-item
            [:span.text-muted "State: "]
            "Massachusetts"]
           [:div.info-item
            [:span.text-muted "Line: "]
            "730 Schoen Center Apt 8"]]
          [:div.card-body
           [:h5.card-title "Identifiers"]
           [:span.info-item
            [:span.text-muted "SSN: "]
            "999-81-4006"]
           [:span.info-item
            [:span.text-muted "MRN: "]
            "803f5907-5427-4930-a093-1a95190de7fd"]
           [:span.info-item
            [:span.text-muted "Driver Licence: "]
            "999-81-4006"]]]
         [:br]
         [:div.card
          [:div.card-header.info-header "Recent encounters"]
          [encounter]
          [encounter]]]]])))

(pages/reg-subs-page
 model/index-card
 (fn [db params]
   [patient-card]))
