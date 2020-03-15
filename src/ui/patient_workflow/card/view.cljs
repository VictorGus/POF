(ns ui.patient-workflow.card.view
  (:require [reagent.core :as r]
            [ui.pages :as pages]
            [ui.styles :as styles]
            [ui.basic-components.info-input :refer [info-input]]
            [ui.patient-workflow.card.model :as model]))

[info-input]

(def card-style
  (styles/style
   [:#patient-card-wrapper
    [:#patient-card
     {:border-radius "8px"
      :border "1px solid rgba(51, 51, 51, 0.1)"}
     [:.patient-name
      {:font-size "24px"
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
        :border "1px solid rgba(51, 51, 51, 0.1)"}
       [:.wrapper
        {:line-height "20px"
         :padding-bottom "15px"}]
       [:.info-item-title
        {:font-size "15px"}]
       [:.info-item-value-wrapper
        {:padding-left "5px"}
        [:input
         {:border 0
          :outline 0
          :background "transparent"
          :border-bottom "1px solid black"}]]]
      [:.card-title {:font-weight "700"
                     :font-size "20px"}]]]]))

(defn patient-card []
  (let [data "TODO"]
    (fn []
      [:div#patient-card-wrapper card-style
       [:div.row
        [:div#patient-card {:class "col-md-6 offset-md-3"}
         [:p.patient-name "Test Test"]
         [:div.patient-info
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Identifiers"]
           [info-input {:title "SSN"
                        :value "999-81-4006"}]
           [info-input {:title "Driver Licence"
                        :value "999-81-4006"}]
           [info-input {:title "MRN"
                        :value "803f5907-5427-4930-a093-1a95190de7fd"}]]]
         [:div.patient-info
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Address"]]
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Telecom"]]]]]])))

(pages/reg-subs-page
 model/index-card
 (fn [db params]
   [patient-card]))
