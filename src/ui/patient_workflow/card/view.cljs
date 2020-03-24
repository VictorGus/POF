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
       {:fill "blue"}]]]]))
(defn encounter []
  [:div.patient-info-item.col-md-12
   {:style {:margin-bottom "10px"}}
   [:div
    [:p.card-title-inner "Encounter for check-up"]
    [:p.text-muted "Ambulatory"]]
   [info-input {:title "Reason"
                :value "Second degree burn"}]
   [info-input {:title "Status"
                :value "Finished"}]])

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
         [:div.patient-info
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Telecom"]
           [info-input {:title "Use"
                        :value "home"}]
           [info-input {:title "Phone number"
                        :value "88005553555"}]]
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Address"]
           [info-input {:title "Country"
                        :value "US"}]
           [info-input {:title "City"
                        :value "Brockton"}]
           [info-input {:title "Postal Code"
                        :value "02301"}]
           [info-input {:title "State"
                        :value "Massachusetts"}]]]
         [:div.patient-info
          [:div.patient-info-item {:class "col-md-12"}
           [:p.card-title "Identifiers"]
           [info-input {:title "SSN"
                        :value "999-81-4006"}]
           [info-input {:title "MRN"
                        :value "803f5907-5427-4930-a093-1a95190de7fd"}]
           [info-input {:title "Driver Licence"
                        :value "999-81-4006"}]]]
         [:div.patient-info
          [:div.patient-info-item {:class "col-md-12"}
           [:p.card-title "Recent encounters"]
           [encounter]
           [encounter]]]]]])))

(pages/reg-subs-page
 model/index-card
 (fn [db params]
   [patient-card]))
