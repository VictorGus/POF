(ns ui.audit.logs.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [baking-soda.core :as b]
            [ui.styles :as styles]
            [ui.pages :as pages]
            [ui.audit.logs.model :as model]))

(def input-style
  (styles/style
   [:#search-input-wrapper
    {:padding-top "15px"
     :padding-left "35px"}]
   [:.not-found {:font-size "22px"}]))

(defn logs-grid []
  (fn []
    [:div#search-input-wrapper input-style
       [b/Container
        [b/Row
         {:styles "height: 48px;"}
         [b/Col
          {:class "col-md-12"}
          [b/Input
           {:type "text"
            :styles "height: 48px;"
            :aria-describedby "inputGroup-sizing-sm"
            :placeholder "Search..."
            :on-change (fn [e]
                         (let [v (-> e .-target .-value)]
                           (js/setTimeout (fn []
                                            (println v))
                                          700)))}]]]]]))

(pages/reg-subs-page
 model/logs
 (fn [_ _]
   [logs-grid]))
