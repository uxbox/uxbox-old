(ns uxbox.streams
  (:require [beicon.core :as b]))

(defonce main-bus
  (b/bus))

(defn on-event
  ([]
   #(do
      (set! (.-ns %) :main)
      (b/push! main-bus %)))
  ([subbus]
   #(do
      (set! (.-ns %) subbus)
      (b/push! main-bus %)))
  ([subbus data]
   #(do
      (set! (.-ns %) subbus)
      (set! (.-data %) data)
      (b/push! main-bus %))))
