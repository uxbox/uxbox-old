(ns uxbox.runner
  (:require [cljs.test :as test]
            [uxbox.test.data-test]))


(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'uxbox.test.data-test))

(set! *main-cli-fn* main)
