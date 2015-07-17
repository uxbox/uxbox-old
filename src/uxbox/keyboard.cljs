(ns uxbox.keyboard
  (:require [uxbox.pubsub :as pubsub]
            [goog.events :as events]
            [goog.dom :as dom])
  (:import [goog.events EventType KeyCodes]
           [goog.ui KeyboardShortcutHandler]))


(def event-keys
  {"DELETE"  [:delete-key-pressed]
   "ESC"     [:set-tool nil]
   "CTRL+B"  [:set-tool :rect]
   "CTRL+E"  [:set-tool :circle]
   "CTRL+L"  [:set-tool :line]
   "SHIFT+Q" [:set-tool :rect]
   "SHIFT+W" [:set-tool :circle]
   "SHIFT+E" [:set-tool :line]
   })

(defn dispatch-key [e]
  (let [event (get event-keys (.-identifier e))]
    (pubsub/publish! event)))


(defn start-keyboard! []
  (let [handler (KeyboardShortcutHandler. js/document)]
    (doseq [[shortcut key] event-keys]
      (.registerShortcut handler shortcut shortcut))

    (events/listen handler
                   KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED
                   dispatch-key)))
