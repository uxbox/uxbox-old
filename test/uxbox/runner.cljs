(ns uxbox.runner
  (:require [cljs.test :as test]
            [uxbox.core-test]))


(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'uxbox.core-test))

(set! *main-cli-fn* main)
