(ns uxbox.runner
  (:require [cljs.test :as test]
            [uxbox.storage.generators-test]))


(enable-console-print!)

(defn main
  []
  (test/run-tests (test/empty-env)
                  'uxbox.storage.generators-test))

(set! *main-cli-fn* main)
