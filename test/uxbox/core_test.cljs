(ns uxbox.core-test
  (:require [cljs.test :as t]))

(t/deftest a-failing-test
  (t/testing "That fails"
    (t/is (= 42 99))))
