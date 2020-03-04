(ns ui.patient-workflow.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
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

     [:.right-item
      {:text-align "right"}]

     [:.patient-record:hover
      {:background-color "#fcfdff"}]]]))

(defn pt-name-to-string [name]
  (str (first (:given (first name))) " " (:family (first name))))

(defn normalize-patient-data [data]
  {:name    (pt-name-to-string (:name data))
   :line    (first (:line (first (:address data))))
   :city    (:city (first (:address data)))
   :state   (:state (first (:address data)))
   :country (:country (first (:address data)))})

(defn patient-grid []
  (let [pt-data (rf/subscribe [::model/patient-data])]
    (fn []
      [:div.patient-grid
       (when (vector? @pt-data)
         (for [item @pt-data]
           (let [normalized-item (normalize-patient-data item)]
             [:a.patient-record
              {:href "#"}
              [:div.icon
               [:img {:src (cond
                             (= (:gender item) "male")
                             "male.svg"
                             (= (:gender item) "female")
                             "female.svg")}]]
              [:div.patient-info
               [:div
                [:b.patient-name (:name normalized-item)]
                [:span.text-muted.pl-2 (:birthDate item)]
                [:div
                 [:span.text-muted
                  "Line:"]
                 [:span.patient-address-value (:line normalized-item)]
                 [:span.text-muted
                  "City:"]
                 [:span.patient-address-value (:city normalized-item)]
                 [:span.text-muted
                  "State:"]
                 [:span.patient-address-value (:state normalized-item)]
                 [:span.text-muted
                  "Country:"]
                 [:span.patient-address-value (:country normalized-item)]]]]
              [:div.right-wrapper
               [:div.right-item
                [:span.text-muted
                 "Social Security Number:"]]
               [:div.right-item
                [:span.text-muted
                 "Driver License:"]]
               [:div.right-item
                [:span.text-muted
                 "Phone:"]]]])))])))

(defn search-input []
  (let [input-cnt (r/atom "")
        sort-order (r/atom false)
        data (rf/subscribe [::model/patient-data])
        dropdown-open? (r/atom false)]
    (fn []
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
                           (when (not (clojure.string/blank? v))
                             (rf/dispatch [::model/search v]))))}]]
         [b/Button {:id "search-btn"
                    :color "outline-primary"} "+ Create"]
         [b/Dropdown {:isOpen @dropdown-open?
                      :on-mouse-over #(reset! dropdown-open? true)
                      :toggle #(swap! dropdown-open? not)
                      :on-mouse-out  #(reset! dropdown-open? false)}
          [b/DropdownToggle {:caret true
                             :color "outline-primary"} "Sort"]
          [b/DropdownMenu
           [b/DropdownItem {:on-click #(do
                                         (swap! sort-order not)
                                         (rf/dispatch [::model/sort-patients @data @sort-order]))}
            "by birthDate"]
           [b/DropdownItem "by name"]]]]
        [patient-grid]]])))
