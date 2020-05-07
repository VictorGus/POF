(ns ui.patient-workflow.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.basic-components.spinner :refer [spinner]]
            [ui.pages :as pages]
            [ui.helper :as helper]
            [ui.zframes.redirect :as redirect]
            [clojure.string :as str]
            [baking-soda.core :as b]
            [ui.patient-workflow.model :as model]
            [ui.styles :as styles]))

(def input-style
  (styles/style
   [:#search-input-wrapper
    {:padding-top "15px"
     :padding-left "35px"}

    [:#search-btn
     {:margin-right "16px"}]

    [:.patient-grid
     [:.icon
      {:height "65px"
       :width "65px"
       :margin-top "10px"
       :padding-right "10px"}

      [:.icon.img
       {:fill "blue"}]]

     [:.patient-record
      {:display "flex"
       :text-decoration "none"
       :color "black"
       :padding "8px 8px"
       :margin-top "12px"
       :border-radius ".25rem"
       :border "1px solid rgba(51, 51, 51, 0.1)"}


      [:.patient-info
       {:display "flex"
        :flex-grow "1"
        :align-items "center"}

       [:.patient-address-label
        #_{:color "white"
         :background-color "#0069d9"
         :box-shadow "0 0 2px rgba(0,0,0,0.5)"
         :padding-right ".5em"
         :padding-bottom ".2em"
         :padding-top ".2em"
         :padding-left ".5em"
         :border-radius "10rem"}]

       [:.patient-address-value
        {:padding "5px 5px"}]

       [:.patient-name
        {:font-size "20px"}]
       ]]

     [:.right-wrapper
      [:.right-item
       {:text-align "right"}
       [:.patient-right-value
        {:padding "5px 5px"}]]]
     [:.patient-record:hover
      {:background-color "#e6f2ff"}]]]
   [:.not-found {:font-size "22px"}]
   [:.marker {:background-color "#bff"}]))

(defn pt-name-to-string [item]
  (str (:given item) " " (:family item)))

(defn highlight [q]
  (when q
    (let [sel (into [] (array-seq (.querySelectorAll js/document ".patient-name, .pl-2, .patient-right-value")))
          q (into [] (remove str/blank? (-> q
                                            (str/replace #"(%20)" " ")
                                            (str/replace #"[`~!@#$%^&*()_|\=?;:'\".<>\{\}\[\]\\\/]" "")
                                            (str/trim)
                                            (str/split #"\s*,| |[+]\s*"))))]
      (mapv (fn [w]
              (mapv (fn [el]
                      (aset el
                            "innerHTML"
                            (str/replace (aget el "innerHTML")
                                         (re-pattern (str "(?i)(" w ")(?!([^<]+)?>)(?!([^&]+)?;)"))
                                         #(str "<span class=\"marker\">" (first %) "</span>"))))
                    sel))
            q))))


(defn patient-grid []
  (let [page-data (rf/subscribe [:patients/index])]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (highlight (:q @page-data)))
      :reagent-render
      (fn []
        [:div.patient-grid
         (when (empty? (:data @page-data))
           [:div.container.text-center.pt-3
            [:span
             [:i.fa.fa-frown-o.fa-2x {:aria-hidden "true"}]
             [:span.not-found " Nothing is found"]]])
         (when (vector? (:data @page-data))
           (for [item (:data @page-data)]
             [:a.patient-record
              {:href (str "/#/patient/" (:id item))}
              [:div.icon
               [:img {:src (cond
                             (= (:gender item) "male")
                             "male.svg"
                             (= (:gender item) "female")
                             "female.svg")}]]
              [:div.patient-info
               [:div
                [:b.patient-name (pt-name-to-string item)]
                [:span.text-muted.pl-2 (:birthdate item)]
                [:div
                 [:span.text-muted
                  "Line:"]
                 [:span.patient-address-value (:line item)]
                 [:span.text-muted
                  "City:"]
                 [:span.patient-address-value (:city item)]
                 [:span.text-muted
                  "State:"]
                 [:span.patient-address-value (:st item)]
                 [:span.text-muted
                  "Country:"]
                 [:span.patient-address-value (:country item)]]]]
              [:div.right-wrapper
               [:div.right-item
                [:span.text-muted
                 "Social Security Number:"]
                [:span.patient-right-value (get-in item [:ids :SB])]
                ]
               [:div.right-item
                [:span.text-muted
                 "Driver License:"]
                [:span.patient-right-value (get-in item [:ids :DL])]]
               [:div.right-item
                [:span.text-muted
                 "Phone:"]
                [:span.patient-right-value (:phone item)]]]])
           )])})))

(defn search-input []
  (let [sort-order (r/atom false)
        page-data (rf/subscribe [:patients/index])
        loading-status (rf/subscribe [::model/loading-status])
        dropdown-open? (r/atom false)]
    (fn []
      [:<>
       [:div#search-input-wrapper input-style
        [b/Container
         [b/Row
          {:styles "height: 48px;"}
          [b/Col
           {:class "col-md-10"}
           [b/Input
            {:type "text"
             :styles "height: 48px;"
             :aria-describedby "inputGroup-sizing-sm"
             :placeholder "Search..."
             :on-change (fn [e]
                          (let [v (-> e .-target .-value)]
                            (js/setTimeout (fn []
                                             (rf/dispatch [::redirect/set-params {:q v}]))
                                           700)))}]]
          [b/Button {:id "search-btn"
                     :color "outline-primary"
                     :on-click #(rf/dispatch [::redirect/redirect
                                              {:uri (helper/make-href (.-host (.-location js/window)) "patients/create")}])} "+ Create"]
          [b/Dropdown {:isOpen @dropdown-open?
                       :on-mouse-over #(reset! dropdown-open? true)
                       :toggle #(swap! dropdown-open? not)
                       :on-mouse-out  #(reset! dropdown-open? false)}
           [b/DropdownToggle {:caret true
                              :color "outline-primary"} "Sort"]
           [b/DropdownMenu
            [b/DropdownItem {:on-click #(do
                                          (swap! sort-order not)
                                          (rf/dispatch [::model/sort-patients (:data @page-data) @sort-order]))}
             "by birthDate"]
            [b/DropdownItem "by name"]]]]
         (when @loading-status
           [spinner])
         (when-not @loading-status
           [patient-grid])]]])))

(pages/reg-subs-page
 model/index
 (fn [db params]
   [search-input]))
