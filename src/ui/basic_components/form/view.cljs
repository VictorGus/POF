(ns ui.basic-components.form.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.basic-components.form.model :as model]))

(defn form-select [items path & [selected]]
  (let [db-value (rf/subscribe [::model/form-values path])
        selected (or selected @db-value)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [path  (last (butlast (aget (.-props this) "argv")))
              value (last (aget (.-props this) "argv"))]
          (when-not (nil? value)
            (rf/dispatch [::model/form-set-value {:path path
                                                  :value (or value @db-value)}]))))
      :reagent-render
      (fn [_ _ _]
        [:select#use-input.form-control.selector
         {:type "text"
          :on-change #(rf/dispatch [::model/form-set-value {:path path
                                                            :value (.-value (.-target %))}])}
         (for [{:keys [value display] :as item} items]
           [:option.selector-item (cond-> {:value (or value @db-value)}
                                    (= selected (or value @db-value))
                                    (assoc :selected "selected"))
            display])])})))

(defn combobox-input [items path & [selected]]
  (let [db-value (rf/subscribe [::model/form-values path])
        selected (or selected @db-value)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [path  (last (butlast (aget (.-props this) "argv")))
              value (last (aget (.-props this) "argv"))]
          (when-not (nil? value)
            (rf/dispatch [::model/form-set-value {:path path
                                                  :value (or value @db-value)}]))))
      :reagent-render
      (fn [_ _ _]
        [:<>
         [:input.custom-select {:list "combobox"
                                :value @db-value
                                :on-change #(rf/dispatch [::model/form-set-value {:path path
                                                                                  :value (.-value (.-target %))}])}]
         [:datalist
          {:type "text"
           :id "combobox"
           :on-change #(rf/dispatch [::model/form-set-value {:path path
                                                             :value (.-value (.-target %))}])}
          (for [{:keys [value display] :as item} items]
            [:option.selector-item (cond-> {:value (or value @db-value)}
                                     (= selected (or value @db-value))
                                     (assoc :selected "selected"))
             display])]])})))

(defn form-input [path & [placeholder value]]
  (let [v (r/atom value)
        db-value (rf/subscribe [::model/form-values path])]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (when-not (nil? value)
          (rf/dispatch [::model/form-set-value {:path path
                                                :value (or value @db-value)}])))
      :reagent-render
      (fn [_ _]
        [:input.form-control {:type "text"
                              :placeholder placeholder
                              :value (or @v @db-value)
                              :on-change #(do
                                            (reset! v (.-value (.-target %)))
                                            (rf/dispatch [::model/form-set-value {:path path
                                                                                  :value (.-value (.-target %))}]))}])})))

(defn form-password [path & [placeholder value]]
  (let [v (r/atom value)
        db-value (rf/subscribe [::model/form-values path])]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (when-not (nil? value)
          (rf/dispatch [::model/form-set-value {:path path
                                                :value (or value @db-value)}])))
      :reagent-render
      (fn [_ _]
        [:input.form-control {:type "password"
                              :placeholder placeholder
                              :value (or @v @db-value)
                              :on-change #(do
                                            (reset! v (.-value (.-target %)))
                                            (rf/dispatch [::model/form-set-value {:path path
                                                                                  :value (.-value (.-target %))}]))}])})))

(defn form-date-input [path & [value]]
  (let [v (r/atom value)
        db-value (rf/subscribe [::model/form-values path])]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (when-not (nil? value)
          (rf/dispatch [::model/form-set-value {:path path
                                                :value (or value @db-value)}])))
      :reagent-render
      (fn [_ _]
        [:input.form-control {:type "date"
                              :value (or @v @db-value)
                              :on-change #(do
                                            (reset! v (.-value (.-target %)))
                                            (rf/dispatch [::model/form-set-value {:path path
                                                                                  :value (.-value (.-target %))}]))}])})))

(defn form-datetime-input [path & [value]]
  (let [v (r/atom value)
        db-value (rf/subscribe [::model/form-values path])]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (rf/dispatch [::model/form-set-value {:path path
                                              :value (or value @db-value)}]))
      :reagent-render
      (fn [_ _]
        [:input.form-control {:type "datetime-local"
                              :value (or @v @db-value)
                              :on-change #(do
                                            (reset! v (.-value (.-target %)))
                                            (rf/dispatch [::model/form-set-value {:path path
                                                                                  :value (.-value (.-target %))}]))}])})))
