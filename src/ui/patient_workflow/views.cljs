(ns ui.patient-workflow.views
  (:require [reagent.core :as r]
            [baking-soda.core :as b]
            [ui.styles :as styles]))

(def input-style
  (styles/style
   [:#search-input-wrapper
    {:padding-top "15px"
     :padding-left "30px"}]))

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
            :on-change #(reset! input-cnt (-> % .target .value))}]]
         [b/Col
          [b/Button {:color "primary"} "+ Create"]]]]])))
