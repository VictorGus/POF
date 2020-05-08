(ns ui.layout
  (:require [reagent.core :as r]
            [ui.styles :as styles]
            [ui.zframes.flash :as flash]))

(def layout-style
  (styles/style
   [:.flashes {:position "fixed" :top "20px" :right "20px" :max-width "500px" :z-index 200}
    [:ul {:padding-left "20px"}]]))

(defn layout [page]
  [:div layout-style
   [flash/flashes]
   page])
