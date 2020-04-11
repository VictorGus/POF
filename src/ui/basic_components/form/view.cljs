(ns ui.basic-components.form.view
  (:require [reagent.core]
            [re-frame.core :as rf]
            [ui.basic-components.form.model :as model]))

(defn form-select [items & [selected]]
  (println selected)
  [:select#use-input.form-control.selector
   {:type "text"
    :on-change (fn [e] (println (.-value (.-target e))))}
   (for [{:keys [value display]} items]
     [:option.selector-item (cond-> {:value value}
                              (= selected value)
                              (assoc :selected "selected"))
      display])])
