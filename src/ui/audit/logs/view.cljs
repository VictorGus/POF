(ns ui.audit.logs.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [baking-soda.core :as b]
            [ui.basic-components.form.view :as basic-form]
            [ui.basic-components.toggle :as toggle]
            [ui.styles :as styles]
            [ui.pages :as pages]
            [ui.audit.logs.form :as form]
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
                                          700)))}]]
         [b/Col
          {:class "col-md-3"}
          [:label.text-muted "Action"]
          [basic-form/form-select [{:display "View"
                                    :value "get"}
                                   {:display "Create"
                                    :value "post"}
                                   {:display "Update"
                                    :value "put"}
                                   {:display "Delete"
                                    :value "delete"}] [form/form-path :identifier :MR]]]
         [b/Col
          {:class "col-md-3"}
          [:label.text-muted "From"]
          [:input.form-control {:type "datetime-local"
                                :on-change #(println (-> % .-target .-value))}]]
         [b/Col
          {:class "col-md-3"}
          [:label.text-muted "To"]
          [:input.form-control {:type "datetime-local"
                                :on-change #(println (-> % .-target .-value))}]]

         [b/Col
          {:class "col-md-3"}
          [:label.text-muted "Govno"]
          [toggle/check-toggle]]

         [b/Col
          {:class "col-md-3"}
          [:button.btn.btn-outline-primary "Refresh"]]]]]))

(pages/reg-subs-page
 model/logs
 (fn [_ _]
   [logs-grid]))
