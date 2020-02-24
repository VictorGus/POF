(ns ui.patient-workflow.views
  (:require [reagent.core :as r]
            [baking-soda.core :as b]
            [ui.styles :as styles]))

(def input-style
  (styles/style
   [:#search-input-wrapper]))

(defn search-input []
  (let [input-cnt (r/atom "")]
    (fn []
      [:div#search-input-wrapper input-style
       [:div.d-flex.justify-content-center
        [b/Row
         [b/Col
          {:col "12"}
          [b/Input
           {:type "text"
            :placeholder "Search..."
            :on-change #(reset! input-cnt (-> % .target .value))}]]
         [b/Col
          [b/Button {:color "primary"} "+ Create"]]]]])))
