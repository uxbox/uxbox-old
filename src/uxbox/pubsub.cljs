(ns uxbox.pubsub
  (:require
   [uxbox.db :as db]
   [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def publisher (atom nil))
(def publication (atom nil))

(defn start-pubsub!
  ([]
   (start-pubsub! (async/chan)))
  ([ch]
   (reset! publisher ch)
   (reset! publication (async/pub ch first))))

(defn publish!
  [msg]
  (async/put! @publisher msg))

(defn register-handler
  [key cb]
  (let [ch (async/chan)]
    (async/sub @publication key ch)
    (go-loop [v (async/<! ch)]
      (if (nil? v)
        (async/close! ch)
        (when-let [new-state (cb @db/app-state v)]
          (reset! db/app-state new-state)
          (recur (async/<! ch)))))))
