(ns ui.basic-components.info-input
  (:require [reagent.core :as r]
            [ui.styles :as styles]))

(def input-style
  (styles/style
   [:.wrapper
    {:line-height "20px"
     :padding-bottom "15px"}
    [:.info-item-title
     {:font-size "15px"
      :box-shadow "0 0 7px rgba(0,0,0,0.5)"}]
    [:.info-item-value-wrapper
     {:padding-left "8px"}

     [:input
      {:border 0
       :outline 0
       :padding "2px"
       :color "#666666"
       :background-color "#f2f2f2"
       :box-shadow "0 0 7px rgba(0,0,0,0.5)"
       ;; :border-bottom "2px solid grey"
       }]]]))

(defn info-input [{:keys [title value]}]
  (let [edit? (r/atom false)
        v (r/atom value)]
    (fn []
      [:div.wrapper
       input-style
       [:span
        [:div.info-item-title {:class "badge badge-primary text-wrap"}
         (str title ":")]
        [:span.info-item-value-wrapper
         [:input#info-item-value {:style {:width (* 8 (+ 1.75 (count @v)))}
                                  :value @v
                                  :read-only true
                                  :on-change (fn [e]
                                               (set! (-> e .-target .-style .-width) (str (* 8 (+ 1 (.-length (.-value (.-target e))))) "px"))
                                               (reset! v (-> e .-target .-value)))}]]]])))

