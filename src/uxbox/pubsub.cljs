(ns uxbox.pubsub
  (:require
   [uxbox.db :as db]
   [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def publisher (atom (async/chan)))
(def publication (atom (async/pub @publisher first)))

(def log (atom []))

(defn publish!
  [msg]
  (swap! log conj msg)
  (async/put! @publisher msg))

(defn register-transition
  [key cb]
  (let [ch (async/chan)]
    (async/sub @publication key ch)
    (go-loop [v (async/<! ch)]
      (if (nil? v)
        (async/close! ch)
        (do
          (if-let [new-state (cb @db/app-state (second v))]
            (reset! db/app-state new-state)
            (.error js/console "The" key "handler didn't return a new version of the state but" (pr new-state)))
          (recur (async/<! ch)))))))

(defn register-effect
  [key cb]
  (let [ch (async/chan)]
    (async/sub @publication key ch)
    (go-loop [v (async/<! ch)]
      (if (nil? v)
        (async/close! ch)
        (do (cb @db/app-state (second v))
            (recur (async/<! ch)))))))

(defn register-event
  [key cb]
  (let [ch (async/chan)]
    (async/sub @publication key ch)
    (go-loop [v (async/<! ch)]
      (if (nil? v)
        (async/close! ch)
        (do (cb @db/app-state (second v))
            (recur (async/<! ch)))))))
