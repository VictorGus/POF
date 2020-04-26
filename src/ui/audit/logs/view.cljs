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
   [:.not-found {:font-size "22px"}]
   [:.filter-form {:z-index "999"
                   :background-color "white"}]))

(defn filter-form [close-fn]
  [:div.container.border.border-top-0.rounded.filter-form
   [:div.row.p-3
    [:div.col-md-2
     [:label.text-muted "Action"]
     [basic-form/form-select [{:display "View"
                               :value "get"}
                              {:display "Create"
                               :value "post"}
                              {:display "Update"
                               :value "put"}
                              {:display "Delete"
                               :value "delete"}] [form/form-path :action]]]
    [:div.col-md-2
     [:label.text-muted "User"]
     [basic-form/form-input [form/form-path :user]]]
    [:div.cold-md-3.mr-2
     [:label.text-muted "From"]
     [:input.form-control {:type "datetime-local"
                           :on-change #(println (-> % .-target .-value))}]]
    [:div.cold-md-3.mr-3
     [:label.text-muted "To"]
     [:input.form-control {:type "datetime-local"
                           :on-change #(println (-> % .-target .-value))}]]

    [:div.cold-md-3
     [:div
      [:label.text-muted "Enable auto update"]]
     [toggle/check-toggle]]]])

(defn input-form []
  (let [show-form? (r/atom false)
        close-fn #(reset! show-form? false)]
    (fn []
      [:div.row
       {:styles "height: 48px;"}
       [:div.subform.col-md-12
        {:tab-index 0
         :on-focus #(reset! show-form? true)
         :on-blur close-fn}
        [b/Input
         {:type "text"
          :styles "height: 48px;"
          :placeholder "Search..."
          :on-change (fn [e]
                       (let [v (-> e .-target .-value)]
                         (js/setTimeout (fn []
                                          (println v))
                                        700)))}]
        (when @show-form?
          [filter-form close-fn])]])))

(defn logs-grid []
  (fn []
    [:div#search-input-wrapper input-style
     [:div.container
      [:iframe {:src "http://localhost:3000/d-solo/2xYT333Wk/requests?tab=advanced&orgId=1&panelId=2&from=1587918034275&to=1587939634275&refresh=1m&theme=light"
                :height "250"
                :width "100%"
                :frameBorder "0"}]
      [input-form]
      [:table.table.table-hover.mt-3
       [:thead
        [:tr
         [:th {:scope "col"} [:span "Status"
                              [:i.fa.fa-sort.ml-1 {:aria-hidden "true"
                                                   :style {:cursor "pointer"}
                                                   :on-click #(println "TODO")}]]]
         [:th {:scope "col"} [:span "TS"
                              [:i.fa.fa-sort.ml-1 {:aria-hidden "true"
                                                   :style {:cursor "pointer"}
                                                   :on-click #(println "TODO")}]]]
         [:th {:scope "col"} "Display"]
         [:th {:scope "col"} [:span "Duration"
                              [:i.fa.fa-sort.ml-1 {:aria-hidden "true"
                                                   :style {:cursor "pointer"}
                                                   :on-click #(println "TODO")}]]]]]
       [:tbody
        [:tr
         [:td [:span.badge.badge-success "200"]]
         [:td "2019-02-13"]
         [:td "Viewing patient Anna13 Marina14 (DL:88080)"]
         [:td "145 ms"]]]]]]))

(pages/reg-subs-page
 model/logs
 (fn [_ _]
   [logs-grid]))
