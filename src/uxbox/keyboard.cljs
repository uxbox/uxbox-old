(ns uxbox.keyboard
  (:require [uxbox.pubsub :as pubsub]
            [goog.events :as events]
            [goog.dom :as dom])
  (:import [goog.events EventType KeyCodes]
           [goog.ui KeyboardShortcutHandler]))


(def event-keys
  {"DELETE"  [:delete-key-pressed]
   "ESC"     [:set-tool nil]
   "CTRL+C"  [:copy-selected]
   "CTRL+V"  [:paste-selected]
   "CTRL+B"  [:set-tool :rect]
   "CTRL+E"  [:set-tool :circle]
   "CTRL+L"  [:set-tool :line]
   "CTRL+T"  [:set-tool :text]
   "SHIFT+Q" [:set-tool :rect]
   "SHIFT+W" [:set-tool :circle]
   "SHIFT+E" [:set-tool :line]
   "SHIFT+T" [:set-tool :text]
   "CTRL+SHIFT+I" [:open-setting-box :figures]
   "CTRL+SHIFT+F" [:open-setting-box :tools]
   "CTRL+SHIFT+C" [:open-setting-box :components]
   "CTRL+SHIFT+L" [:open-setting-box :layers]
   "CTRL+G" [:toggle-grid]
   "CTRL+UP" [:move-layer-up]
   "CTRL+DOWN" [:move-layer-down]
   "CTRL+SHIFT+UP" [:move-layer-to-bottom]
   "CTRL+SHIFT+DOWN" [:move-layer-to-top]
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
