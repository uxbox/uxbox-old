(ns uxbox.ui.keyboard
  (:require [goog.events :as events])
  (:import [goog.events EventType KeyCodes]
           [goog.ui KeyboardShortcutHandler]))


(defn is-keycode?
  [keycode]
  (fn [e]
    (= (.-keyCode e) keycode)))

(def esc? (is-keycode? 27))
(def enter? (is-keycode? 13))

;; (def event-keys
;;   {"DELETE"  [:delete-key-pressed]
;;    "ESC"     [:set-tool nil]
;;    "CTRL+C"  [:copy-selected]
;;    "CTRL+V"  [:paste-selected]
;;    "CTRL+B"  [:set-tool :rect]
;;    "CTRL+E"  [:set-tool :circle]
;;    "CTRL+L"  [:set-tool :line]
;;    "SHIFT+Q" [:set-tool :rect]
;;    "SHIFT+W" [:set-tool :circle]
;;    "SHIFT+E" [:set-tool :line]
;;    "CTRL+SHIFT+I" [:open-setting-box :icons]
;;    "CTRL+SHIFT+F" [:open-setting-box :tools]
;;    "CTRL+SHIFT+C" [:open-setting-box :components]
;;    "CTRL+SHIFT+L" [:open-setting-box :layers]
;;    "CTRL+G" [:toggle-grid]
;;    "CTRL+UP" [:move-layer-up]
;;    "CTRL+DOWN" [:move-layer-down]
;;    "CTRL+SHIFT+UP" [:move-layer-to-bottom]
;;    "CTRL+SHIFT+DOWN" [:move-layer-to-top]
;;    "SHIFT+I" [:zoom-in]
;;    "SHIFT+0" [:zoom-reset]
;;    "SHIFT+O" [:zoom-out]
;;    })

;; (defn dispatch-key [e]
;;   (let [event (get event-keys (.-identifier e))]
;;     (pubsub/publish! event)))

;; (defn start-keyboard! []
;;   (let [handler (KeyboardShortcutHandler. js/document)]
;;     (doseq [[shortcut key] event-keys]
;;       (.registerShortcut handler shortcut shortcut))

;;     (events/listen handler
;;                    KeyboardShortcutHandler.EventType.SHORTCUT_TRIGGERED
;;                    dispatch-key)))
