(ns uxbox.runner
  (:require [cljs.test :as test]
            [uxbox.test.data-test]
            [uxbox.test.queries-test]
            [uxbox.test.projects.data-test]
            [uxbox.test.shapes.data-test]
            [uxbox.test.streams-test]))

(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'uxbox.test.data-test
                  'uxbox.test.projects.data-test
                  'uxbox.test.shapes.data-test
                  'uxbox.test.queries-test
                  'uxbox.test.streams-test))

(set! *main-cli-fn* main)
