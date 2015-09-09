(ns uxbox.runner
  (:require [cljs.test :as test]))


(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)))

(set! *main-cli-fn* main)
