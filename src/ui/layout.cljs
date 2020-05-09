(ns ui.layout
  (:require [reagent.core :as r]
            [ui.styles :as styles]
            [garden.units :refer [px]]
            [ui.zframes.flash :as flash]))

(def layout-style
  (let [left-bar 100]
    (styles/style
     [:#layout
      [:#menu
       {:position "absolute"
        :padding "15px"
        :top (px 20)
        :left 0
        :width (px left-bar)}
       [:.menu-item
        {:color "black"
         :display "block"
         :position "relative"
         :width "66px"
         :margin-bottom "10px"
         :opacity "0.4"}
        [:div.label {:font-size "16px"
                     :text-align "center"}]
        [:i {:font-size "34px"
             :display "inline-block"
             :width "66px"
             :height "66px"
             :line-height "63px"
             :text-align "center"
             :border "1px solid black"
             :border-radius "50%"} ]
        [:&:hover {:opacity 0.6}]]]
      [:#content
       {:margin-left (px left-bar)
        :padding-right "20px"
        :padding-top "10px"}]]
     [:.flashes {:position "fixed" :top "20px" :right "20px" :max-width "500px" :z-index 200}
      [:ul {:padding-left "20px"}]])))

(defn menu []
  [:div#menu
   [:a.menu-item {:href "#"}
    [:i.fas.fa-user-friends]
    [:div.label "Patients"]]
   [:a.menu-item {:href "#/audit/logs"}
    [:i.fas.fa-chart-line]
    [:div.label "Monitor"]]
   ])

(defn layout [page]
  [:div.h-100 layout-style
   [flash/flashes]
   [:div#layout
    [menu]
    [:div#content page]]
   ])
