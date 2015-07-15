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
  (let [app-node (dom/getElement "app")]
    (events/listen app-node EventType.KEYPRESS dispatch-key)))
