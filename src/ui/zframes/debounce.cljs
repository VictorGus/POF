(ns ui.zframes.debounce
  (:require [re-frame.core :as rf]))

(defonce debounce-events (atom {}))

(defn debounce [[nm :as ev] & ms]
  (when-let [h (get @debounce-events nm)]
    (js/clearTimeout h))
  (swap! debounce-events assoc nm
         (js/setTimeout (fn [] (rf/dispatch ev)) (or ms 400))))
