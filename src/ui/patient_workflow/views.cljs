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
        :align-items "center"}]]
     [:.patient-record:hover {:background-color "#fcfdff"}]]]))

(defn patient-grid []
  (let [pt-data (rf/subscribe [::model/patient-data])] ;;TODO
    (fn []
      [:div.patient-grid
       (for [item @pt-data]
         [:a.patient-record
          {:href "#"}
          [:div.icon
           [:img {:src (cond
                         (= (:gender item) "male") ;;TODO
                         "male.svg"
                         (= (:gender item) "female")
                         "female.svg")}]]
          [:div.patient-info
           [:div
            [:b (:name item)]
            [:span.text-muted.pl-2 (:birthDate item)]
            [:div ;;TODO
             (:address item)]]]])])))

(defn search-input []
  (let [input-cnt (r/atom "")
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
            :on-change #(reset! input-cnt (-> % .-target .-value))}]]
         [b/Button {:id "search-btn"
                    :color "outline-primary"} "+ Create"]
         [b/Dropdown {:isOpen @dropdown-open?
                      :on-mouse-over #(reset! dropdown-open? true)
                      :toggle #(swap! dropdown-open? not)
                      :on-mouse-out  #(reset! dropdown-open? false)}
          [b/DropdownToggle {:caret true
                             :color "outline-primary"} "Sort"]
          [b/DropdownMenu
           [b/DropdownItem "by birthDate"]
           [b/DropdownItem "by name"]]]]
        [patient-grid]]])))
