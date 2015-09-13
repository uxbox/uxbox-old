(require '[cljs.build.api :as b]
         '[cljs.repl :as repl]
         '[cljs.repl.node :as node])

;; Base compiler configuration
(def compiler-config {:main 'uxbox.core
                      :asset-path "/js/compiled/out"
                      :output-to "resources/public/js/compiled/uxbox.js"
                      :output-dir "resources/public/js/compiled/out"
                      :source-map-timestamp true
                      :warnings {:single-segment-namespace false}
                      :externs ["externs/bacon.js"]
                      :foreign-libs [{:file "http://cdnjs.cloudflare.com/ajax/libs/bacon.js/0.7.73/Bacon.js"
                                      :file-min "http://cdnjs.cloudflare.com/ajax/libs/bacon.js/0.7.73/Bacon.min.js"
                                      :provides ["bacon"]}]})

;;
;; Development
;;

(def dev-config
  {:id "dev"
   :source-paths ["src" "test"]

   ;;:figwheel { :on-jsload "uxbox.core/on-js-reload" }

   :compiler compiler-config})

(defn build-dev
  []
  (b/build "dev" dev-config))

#_(build-dev)

;;
;; Testing
;;

(def test-config
  {:id "test"
   :source-paths ["src" "test"]
   :notify-command ["node" "out/tests.js"]
   :compiler (merge compiler-config {:main 'uxbox.runner
                                     :asset-path "out"
                                     :output-to "out/tests.js"
                                     :output-dir "out"
                                     :source-map true
                                     :static-fns true
                                     :cache-analysis false
                                     :optimizations :none
                                     :target :nodejs
                                     :pretty-print true})})

(defn build-test
  []
  (b/build "test" (:compiler test-config)))

(defn watch-test
  []
  (b/watch (apply b/inputs (:source-paths test-config))
           (:compiler test-config)))

#_(watch-test)

;;
;; Production
;;

(def prod-config
  {:id "prod"
   :source-paths ["src"]
   :compiler (merge compiler-config {:output-to "resources/public/js/compiled/uxbox.js"
                                     :optimizations :advanced
                                     :pretty-print false})})

(defn build-prod
  []
  (b/build "prod" prod-config))

(defn node-repl
  []
  (repl/repl (node/repl-env)
             :output-dir "out"
             :cache-analysis true))
