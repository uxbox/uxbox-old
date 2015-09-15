(defproject uxbox "0.1.0-SNAPSHOT"
  :description "UXBox client"
  :url "http://uxbox.github.io"
  :license {:name ""
            :url ""}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 ;; String
                 [funcool/cuerdas "0.5.0"]
                 ;; Rendering
                 [rum "0.3.0"]
                 [cljsjs/react-with-addons "0.13.3-0"]
                 ;; Compositional Event Streams
                 ;;[funcool/tocino "0.1.0"]
                 ;; Datetime
                 [cljsjs/moment "2.10.6-0"]
                 ;; Routing
                 [secretary "1.2.3" :exclusions [org.clojure/clojurescript]]
                 ;; Asynchrony & Communication
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;; Storage and Querying
                 [datascript "0.11.6"]
                 ;; Persistence
                 [funcool/hodgepodge "0.1.4"]
                 ;; Misc
                 [funcool/cats "1.0.0"]
                 ;; Development
                 [ring "1.4.0"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.3.9"]
            [hiccup-bridge "1.0.1"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src" "test"]

              :figwheel { :on-jsload "uxbox.core/on-js-reload" }

              :compiler {:main uxbox.core
                         :asset-path "/js/compiled/out"
                         :output-to "resources/public/js/compiled/uxbox.js"
                         :output-dir "resources/public/js/compiled/out"
                         :source-map-timestamp true
                         :warnings {:single-segment-namespace false}
                         :foreign-libs [{:file "./node_modules/baconjs/dist/Bacon.js"
                                         :provides ["bacon"]}]}
              }

             {:id "test"
              :source-paths ["src" "test"]
              :notify-command ["node" "out/tests.js"]
              :compiler {:output-to "out/tests.js"
                          :output-dir "out"
                          :source-map true
                          :static-fns true
                          :cache-analysis false
                          :main uxbox.runner
                          :optimizations :none
                          :target :nodejs
                          :pretty-print true
                          :warnings {:single-segment-namespace false}
                          :foreign-libs [{:file "./node_modules/baconjs/dist/Bacon.js"
                                          :provides ["bacon"]}]}}

             {:id "min"
              :source-paths ["src"]
              :compiler {:output-to "resources/public/js/compiled/uxbox.js"
                         :main uxbox.core
                         :optimizations :advanced
                         :pretty-print false
                         :warnings {:single-segment-namespace false}
                         :foreign-libs [{:file "./node_modules/baconjs/dist/Bacon.js"
                                         :provides ["bacon"]}]}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             :ring-handler uxbox.ring/index

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
