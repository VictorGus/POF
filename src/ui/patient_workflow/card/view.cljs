(ns ui.patient-workflow.card.view
  (:require [reagent.core :as r]
            [ui.pages :as pages]
            [ui.styles :as styles]
            [ui.patient-workflow.card.model :as model]))

(def card-style
  (styles/style
   [:#patient-card-wrapper
    [:#patient-card
     {:border-radius "8px"
      :border "1px solid rgba(51, 51, 51, 0.1)"}
     [:.patient-name
      {:font-size "20px"
       :font-weight "900"}]
     [:.info-item
      [:#label-item
       {:height "23px"
        :font-size "15px"}]]
     [:.patient-info
      {:display "flex"}
      [:.patient-info-item
       {:border-radius "8px"
        :margin-right "5px"
        :border "1px solid rgba(51, 51, 51, 0.1)"}]
      [:.card-title {:font-weight "700"
                     :font-size "19"}]]]]))

(defn patient-card []
  (let [data "TODO"]
    (fn []
      [:div#patient-card-wrapper card-style
       [:div.row
        [:div#patient-card {:class "col-md-6 offset-md-3"}
         [:p.patient-name "Test Test"]
         [:div.patient-info
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Address"]]
          [:div.patient-info-item {:class "col-md-6"}
           [:p.card-title "Telecom"]]]]
        ]])))

(pages/reg-subs-page
 model/index-card
 (fn [db params]
   [patient-card]))
