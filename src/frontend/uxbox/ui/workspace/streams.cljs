(ns uxbox.ui.workspace.streams
  (:require
   [uxbox.streams :as s]
   [uxbox.ui.streams.mouse :as mouse]))

;; Buses

(def workspace-scroll-bus
  (s/bus))

(def workspace-keyboard-bus
  (s/bus))

(def selected-tool-bus
  (s/bus))

(def active-grid-bus
  (s/bus))

(def zoom-bus
  (s/bus))

;; Transformers

(defn- scroll-event
  [e]
  (let [t (.-target e)]
    {:top (.-scrollTop t)
     :left (.-scrollLeft t)}))

(defn- keyboard-event [e]
  (case (.-identifier e)
    ;; "DELETE"  [:delete-key-pressed]
    "ESC"     [:set-tool nil]
    ;; "CTRL+C"  [:copy-selected]
    ;; "CTRL+V"  [:paste-selected]
    "CTRL+B"  [:set-tool :rect]
    "CTRL+E"  [:set-tool :circle]
    "CTRL+L"  [:set-tool :line]
    "SHIFT+Q" [:set-tool :rect]
    "SHIFT+W" [:set-tool :circle]
    "SHIFT+E" [:set-tool :line]
    ;; "CTRL+SHIFT+I" [:open-setting-box :icons]
    ;; "CTRL+SHIFT+F" [:open-setting-box :tools]
    ;; "CTRL+SHIFT+C" [:open-setting-box :components]
    ;; "CTRL+SHIFT+L" [:open-setting-box :layers]
    "CTRL+G" [:toggle-grid]
    ;; "CTRL+UP" [:move-layer-up]
    ;; "CTRL+DOWN" [:move-layer-down]
    ;; "CTRL+SHIFT+UP" [:move-layer-to-bottom]
    ;; "CTRL+SHIFT+DOWN" [:move-layer-to-top]
    "SHIFT+I" [:zoom-in]
    "SHIFT+0" [:zoom-reset]
    "SHIFT+O" [:zoom-out]
    [:none]))

;; Streams

(def workspace-top-scroll-stream
  (s/dedupe (s/map :top workspace-scroll-bus)))

(def workspace-left-scroll-stream
  (s/dedupe (s/map :left workspace-scroll-bus)))

(def workspace-keyboard-select-tool-stream
  (s/map #(conj [] (second %))
         (s/filter #(= (first %) :set-tool) workspace-keyboard-bus)))

(def selected-tool-stream
  (s/to-event-stream selected-tool-bus))

(def keyboard-toggle-grid-stream
  (s/filter #{:toggle-grid}
            (s/map first workspace-keyboard-bus)))

(def active-grid-stream
  (s/map #(not @grid?) active-grid-bus))

(def keyboard-zoom-stream
  (s/filter #{:zoom-in :zoom-out :zoom-reset}
            (s/map first workspace-keyboard-bus)))

(def zoom-stream
  (s/map (fn [action]
           (case action
             :zoom-in (max 0.01 (+ @zoom (* @zoom 0.1)))
             :zoom-out (max 0.01 (- @zoom (* @zoom 0.1)))
             :zoom-reset 1
             @zoom))
          zoom-bus))

;; Atoms

(defonce scroll-top (s/pipe-to-atom workspace-top-scroll-stream))
(defonce scroll-left (s/pipe-to-atom workspace-left-scroll-stream))
(defonce selected-tool (s/pipe-to-atom selected-tool-stream))
(defonce grid? (s/pipe-to-atom active-grid-stream))
(defonce zoom (s/pipe-to-atom (atom 1) zoom-stream))

;; Handlers

(defn on-workspace-scroll
  [e]
  (s/push! workspace-scroll-bus
           (scroll-event e)))

(defn on-workspace-keypress
  [e]
  (s/push! workspace-keyboard-bus
           (keyboard-event e)))

(defn toggle-tool!
  [tool]
  (if (= @selected-tool tool)
    (s/push! selected-tool-bus :none)
    (s/push! selected-tool-bus tool)))

(defn deselect-tool!
  []
  (s/push! selected-tool-bus :none))

(defn toggle-grid!
  [e]
  (s/push! active-grid-bus :toggle-grid))

;; Properties

(def tool-selected?
  (s/to-property (s/map #(not= % :none)
                        selected-tool-stream)))

;; Connections

(s/plug! selected-tool-bus workspace-keyboard-select-tool-stream)
(s/plug! active-grid-bus keyboard-toggle-grid-stream)
(s/plug! zoom-bus keyboard-zoom-stream)
