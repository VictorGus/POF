(ns ui.patient-workflow.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [baking-soda.core :as b]
            [ui.patient-workflow.model :as model]
            [ui.styles :as styles]))

(def input-style
  (styles/style
   [:#search-input-wrapper
    {:padding-top "15px"
     :padding-left "35px"}
    [:.patient-grid
     [:.patient-record
      {:display "flex"
       :padding "12px 12px"
       :border-bottom "1px solid rgba(51, 51, 51, 0.1)"}
      [:.patient-info
       {:display "flex"
        :flex-grow "1"
        :align-items "center"}]]]]))

(defn patient-grid []
  (let [pt-data (rf/subscribe [::model/patient-data])]
    (fn []
      [:div.patient-grid
       (for [item @pt-data]
         [:a.patient-record
          [:div.icon ]
          [:div.patient-info
           [:div
            [:b (:name item)]
            [:span.text-muted.pl-2 (:birthDate item)]
            [:div
             (:address item)]]]])])))

(defn search-input []
  (let [input-cnt (r/atom "")]
    (fn []
      [:div#search-input-wrapper input-style
       [b/Container
        {:class "container-md"}
        [b/Row
         [b/Col
          {:class "col-md-9"}
          [b/Input
           {:type "text"
            :placeholder "Search..."
            :on-change #(reset! input-cnt (-> % .-target .-value))}]]
         [b/Col
          [b/Button {:color "primary"} "+ Create"]]]
        [patient-grid]]])))
