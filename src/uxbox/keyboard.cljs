(ns uxbox.keyboard
  (:require [uxbox.pubsub :as pubsub]
            [goog.events :as events]
            [goog.dom :as dom])
  (:import [goog.events EventType KeyCodes]))

(def key-event
  {127 :delete-key-pressed})

(defn dispatch-key [e]
  (when-let [event-id (get key-event (.-keyCode e))]
    (pubsub/publish! [event-id])))

(defn start-keyboard! []
  (let [app-node (aget (.getElementsByTagName js/document "html") 0)]
    (events/listen app-node EventType.KEYPRESS dispatch-key)))
