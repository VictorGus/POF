(ns ui.audit.logs.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [baking-soda.core :as b]
            [ui.basic-components.form.view :as basic-form]
            [ui.basic-components.toggle :as toggle]
            [ui.styles :as styles]
            [ui.pages :as pages]
            [ui.helper :as h]
            [ui.audit.logs.form :as form]
            [ui.audit.logs.model :as model]))

(def input-style
  (styles/style
   [:#search-input-wrapper
    {:padding-top "15px"
     :padding-left "35px"}]
   [:.not-found {:font-size "22px"}]
   [:.filter-form {:z-index "10"
                   :background-color "white"}]
   [:.grid-table
    [:th {:padding "4px 16px"
          :font-weight "900 !important"}]
    [:td {:white-space "nowrap"
          :overflow "hidden"
          :text-overflow "ellipsis"
          :padding "8px 16px"
          :max-width "310px"}]]))

(def form-style
  (styles/style
   [:.refresh-btn {:position "absolute"
                   :top "9px"
                   :z-index "999"
                   :outline "none"
                   :right "24px"}
    [:&:hover {:background-color "#fafafa" :cursor "pointer" :border-radius "25px"}]]))

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
     [basic-form/form-datetime-input [form/form-path :gte]
      #_(h/day-ago-with-time)]]
    [:div.cold-md-3.mr-3
     [:label.text-muted "To"]
     [basic-form/form-datetime-input [form/form-path :lte]
      #_(h/tomorrow-date-with-time)]]
    [:div.pl-3.pt-3
     [:div.cold-md-3
      [:div
       [:label.text-muted "Enable auto update"]]
      [toggle/check-toggle]]]]])

(defn input-form []
  (let [show-form? (r/atom false)
        close-fn #(reset! show-form? false)]
    (fn []
      [:div.row form-style
       [:div.col-md-12
        [:i.fa.fa-retweet.refresh-btn
         {:on-click #(rf/dispatch [::model/send-req])}]
        [:div.subform
         {:tab-index 0
          :on-focus #(reset! show-form? true)
          :on-blur close-fn}
         [b/Input
          {:type "text"
           :styles "height: 48px; position: relative;"
           :placeholder "Search..."
           :on-change (fn [e]
                        (let [v (-> e .-target .-value)]
                          (js/setTimeout (fn []
                                           (println v)) 700)))}]
         (when @show-form?
           [filter-form close-fn])]]])))

(defn logs-grid [data]
  (fn [data]
    [:div#search-input-wrapper input-style
     [:div.container
      [:iframe {:src "http://localhost:3000/d-solo/jXNZW7eZz/requests?orgId=1&theme=light&panelId=2&refresh=1m"
                :height "250"
                :width "100%"
                :frameBorder "0"}]
      [input-form]
      [:table.table.table-hover.mt-3.grid-table
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
        (for [item data]
          [:tr
           [:td (if (#{200 201} (:st item))
                  [:span.badge.badge-success (:st item)]
                  [:span.badge.badge-danger (:st item)])]
           [:td (:ts item)]
           [:td (str (:l_m item) "patient " (:l_body item))]
           [:td (str (:d item) " ms")]])]]]]))

(pages/reg-subs-page
 model/logs
 (fn [{:keys [data] :as page} params]
   [logs-grid data]))
