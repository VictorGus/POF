(ns ui.styles
  (:require [garden.core :as gc]))

(defn style [& styles]
  [:style (gc/css styles)])
