(ns user
  (:require [figwheel.main.api :as repl]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [resource-response content-type not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [app.core :as server]))

(def route-set #{"/"})

(defn initial-handler [req]
  (println (keys req))
  (or
   (when (route-set (:uri req))
     (some-> (resource-response "index.html" {:root "public"})
             (content-type "text/html; charset=utf-8")))
   (not-found "Not found")))

(defn mk-handler [dispatch]
  (fn [req]
    (let [resp (dispatch req)]
      resp)))

(def handler (-> initial-handler mk-handler (wrap-defaults site-defaults)))

(handler {})

(def figwheel-options
  {:id "app"
   :options {:main 'ui.dev
             :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
             :preloads        ['day8.re-frame-10x.preload]
             :output-to "resources/public/js/app.js"
             :output-dir "resources/public/js/out"}
   :config {:watch-dirs ["src"]
            :mode :serve
            :ring-handler #'handler
            :ring-server-options {:port 3449}}})

(defn run-ui [opts]
  (repl/start  opts))

(defn run-back [opts]
  (repl/start  opts))

(defn start []
  (run-ui figwheel-options)
  (server/start-server))

(defn stop []
  (repl/stop "app")
  (server/stop-server)
  (println "\nUI stopped"))

(defonce state
  (future (start)))

(comment
  (start)
  (stop)

  (repl/cljs-repl "app")

  )
