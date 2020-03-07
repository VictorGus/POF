(ns ui.basic-components.spinner
  (:require [ui.styles :as styles]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(def spinner-style
  (styles/style
   [:.spinner-area
    {:padding-top "20px"}]))

(defn spinner []
  (let []
    (fn []
      [:div.spinner-area spinner-style
       [:div.text-center
        [:span
         [:div.spinner-grow.text-primary
          [:span.sr-only "Loading..."]]
         [:div.spinner-grow.text-primary
          [:span.sr-only "Loading..."]]
         [:div.spinner-grow.text-primary
          [:span.sr-only "Loading..."]]]]])))
