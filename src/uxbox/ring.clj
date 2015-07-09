(ns uxbox.ring
  (:require [ring.util.response :refer [resource-response]]))

(defn index
  [_]
  (resource-response "index.html" {:root "public"}))
