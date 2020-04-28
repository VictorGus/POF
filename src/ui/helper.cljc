(ns ui.helper
  (:require [re-frame.core :as rf]
            [route-map.core :as route-map]
            [chrono.core :as ch]
            [chrono.now :as now]
            [clojure.string :as str]
            [ui.routes :as routes]))

(def iso-fmt  [:year "-" :month "-" :day "T" :hour ":" :min ":" :sec])
(def iso-date [:year "-" :month "-" :day])

(defn day-ago []
  (ch/format (ch/- (now/local) {:day 1}) iso-date))

(defn day-ago-with-time []
  (ch/format (ch/- (now/local) {:day 1}) iso-fmt))

(defn tomorrow-date []
  (ch/format (ch/+ (now/local) {:day 1}) iso-date))

(defn tomorrow-date-with-time []
  (ch/format (ch/+ (now/local) {:day 1}) iso-fmt))

(defn vec-search [value coll]
  (first (filter (fn [el]
                   (let [target? (cond (map? el)
                                       (first (filter (fn [el-inner]
                                                        (if (or (map? el-inner) (vector? el-inner))
                                                          (vec-search value el-inner)
                                                          (= el-inner value))) (vals el)))
                                       (vector? el)
                                       (vec-search value el)
                                       :else
                                       (= el value))]
                     target?))
                 (cond-> coll (map? coll) vals))))

(defn href-without-domain [uri]
  (let [slash #?(:clj (char 47)
                 :cljs (first (.fromCharCode js/String 47)))
        slashes (get (frequencies uri) slash)]
    (str "/" (str/join "/" (take-last (- slashes 3) (str/split uri (re-pattern (str slash))))))))

(defn make-href [uri item]
  (str (href-without-domain uri) "/" item))

(defn make-back-href [uri]
  (as-> (href-without-domain uri) uri (str/split uri (re-pattern "/")) (butlast uri) (str/join "/" uri)))

(defn flatten-map [m & [path]]
  (reduce-kv
   (fn [acc k v]
     (let [path (conj (or path []) k)]
       (if (coll? v)
         (concat acc (flatten-map v path))
         (conj acc path))))
   []
   m))

(defn vec-remove [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn convert-to-iso [date]
  (let [iso [:year "-" :month "-" :day "T" :hour ":" :min ":" :sec]]
    (ch/format (ch/parse date) iso)))
