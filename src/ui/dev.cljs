(ns ^:figwheel-no-load ui.dev
  (:require [ui.core :as core]
            [devtools.core :as devtools]))

(devtools/install!)
(enable-console-print!)
(core/mount-root)
