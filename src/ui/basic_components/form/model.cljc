(ns ui.basic-components.form.model
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
 ::init-form
 (fn [{db :db} [_ path form-schema]]
   {:db (assoc-in db [path :schema] form-schema)}))

(rf/reg-event-fx
 ::form-set-value
 (fn [{db :db} [_ {:keys [path value]}]]
   (let [form-schema (get-in db [(first path) :schema])
         butlast-value (get-in db (butlast path))
         typo (get-in form-schema [(last (butlast path)) :type])]
     {:db (assoc-in db path value)}
     #_(cond
       (and (= "array" typo) (nil? butlast-value))
       {:db (assoc-in db (butlast path) [{(last path) value}])}
       (and (= "array" typo) (empty? (filter
                                      #(nil? (get % (last path)))
                                      butlast-value)))
       {:db (update-in db (butlast path) conj {(last path) value})}
       (= "array" typo)
       {:db (assoc-in db (conj (butlast path)
                               #?(:clj (.indexOf butlast-value (->> butlast-value
                                                                    (filter #(nil? (get % (last path))))
                                                                    last))
                                  :cljs (.indexOf (to-array butlast-value) (->> butlast-value
                                                                                (filter #(nil? (get % (last path))))
                                                                                last)))
                               (last path))
                      value)}
       :else
       {:db (assoc-in db path value)}))))
