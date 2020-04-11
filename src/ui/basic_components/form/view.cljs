(ns ui.basic-components.form.view
  (:require [reagent.core]
            [re-frame.core :as rf]
            [ui.basic-components.form.model :as model]))

(defn form-select [items path & [selected]]
  [:select#use-input.form-control.selector
   {:type "text"
    :on-change #(rf/dispatch [::model/form-set-value {:path path
                                                      :value (.-value (.-target %))}])}
   (for [{:keys [value display]} items]
     [:option.selector-item (cond-> {:value value}
                              (= selected value)
                              (assoc :selected "selected"))
      display])])

(defn form-input [path & [placeholder value]]
  [:input.form-control {:type "text"
                        :placeholder placeholder
                        :value value
                        :on-change #(rf/dispatch [::model/form-set-value {:path path
                                                                          :value (.-value (.-target %))}])}])
