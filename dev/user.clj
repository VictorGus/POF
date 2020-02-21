(ns user
  (:require [figwheel.main.api :as repl]))

(def figwheel-options
  {:id "app"
   :options {:main 'ui.dev

             ;; :foreign-libs [{:file "resources/public/js/monaco/monaco.js"
             ;;                 :provides ["jslib.monaco"]}
             ;;                {:file "resources/public/js/monaco/yaml.js"
             ;;                 :provides ["jslib.monaco.yaml"]}
             ;;                {:file "resources/public/js/monaco/clojure.js"
             ;;                 :provides ["jslib.monaco.clojure"]}
             ;;                {:file "resources/public/js/icons.js"
             ;;                 :provides ["jslib.icons"]}]

             :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
             :preloads        ['day8.re-frame-10x.preload]
             :output-to "resources/public/js/app.js"
             :output-dir "resources/public/js/out"}
   :config {:watch-dirs ["src"]
            :mode :serve
            :ring-server-options {:port 3449}}})

(defn run-ui [opts]
  (repl/start  opts))

(defn run-back [opts]
  (repl/start  opts))

(defn start []
  (run-ui figwheel-options)
  #_(server/start-server))

(defn stop []
  (repl/stop "app")
  ;; (server/stop-server)
  (println "\nUI stopped"))

(defonce state
  (future (start)))

(comment
  (start)
  (stop)

  (repl/cljs-repl "app")
  )
