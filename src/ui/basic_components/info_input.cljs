(ns ui.basic-components.info-input
  (:require [reagent.core :as r]
            [ui.styles :as styles]))

(def input-style
  (styles/style
   [:.wrapper
    {:line-height "20px"
     :padding-bottom "15px"}
    [:.info-item-value-wrapper
     {:padding-left "5px"}
     [:.input-info
      {:border 0
       :outline 0
       :background "transparent"
       :border-bottom "1px solid grey"}]]]))

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
         [:span.input-info {:class "text-muted"}
          @v]
         #_[:input#info-item-value {:value @v
                                    :on-change (fn [e]
                                               (set! (-> e .-target .-style .-width) (str (* 8 (+ 1 (.-length (.-value (.-target e))))) "px"))
                                               (reset! v (-> e .-target .-value)))}]]]])))

