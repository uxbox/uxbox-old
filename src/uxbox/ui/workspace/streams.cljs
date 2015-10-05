(ns uxbox.ui.workspace.streams
  (:require
   [uxbox.streams :refer [main-bus]]
   [beicon.core :as b]))

;; Transformers

(defn- shortcut-event [e]
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

;;;; Main workspace stream
(def workspace-stream
  (b/filter #(= (.-ns %) :workspace) main-bus))

;;;; Clicks streams
(def click-stream
  (b/filter #(= (.-type %) "click") workspace-stream))

(def click-toggle-grid-stream
  (b/map #(keyword :toggle-grid)
         (b/filter #(= (:type (.-data %)) :toggle-grid) click-stream)))

(def click-toggle-tool-stream
  (b/map #(:tool (.-data %))
         (b/filter #(= (:type (.-data %)) :toggle-tool) click-stream)))

(def click-zoom-stream
  (b/map #(:zoom-type (.-data %))
         (b/filter #(= (:type (.-data %)) :zoom) click-stream)))

;;;; Scroll streams
(def scroll-stream
  (b/map (fn [e]
           (let [t (.-target e)]
             {:top (.-scrollTop t)
              :left (.-scrollLeft t)}))
         (b/filter #(= (.-type %) "scroll") workspace-stream)))

(def top-scroll-stream
  (b/dedupe (b/map :top scroll-stream)))

(def left-scroll-stream
  (b/dedupe (b/map :left scroll-stream)))

;;;; Shortcuts Streams
(def shortcuts-stream
  (b/map shortcut-event
         (b/filter #(= (.-type %) "shortcut") workspace-stream)))

(def shortcuts-toggle-tool-stream
  (b/map #(conj [] (second %))
         (b/filter #(= (first %) :set-tool) shortcuts-stream)))

(def shortcuts-toggle-grid-stream
  (b/filter #{:toggle-grid}
            (b/map first shortcuts-stream)))

(def shortcuts-zoom-stream
  (b/filter #{:zoom-in :zoom-out :zoom-reset}
            (b/map first shortcuts-stream)))

;;;; Final Streams

(def selected-tool-stream
  ;; TODO Generate it correctly as a combination of streams
  (b/map #(if (= @selected-tool %) [:none] %)
         (b/merge shortcuts-toggle-tool-stream
                  click-toggle-tool-stream)))

(def grid?-stream
  ;; TODO Generate it correctly as a combination of streams
  (b/map #(not @grid?)
         (b/merge shortcuts-toggle-grid-stream
                  click-toggle-grid-stream)))

(def zoom-stream
  (b/map (fn [action]
           (case action
             :zoom-in (max 0.01 (+ @zoom (* @zoom 0.1)))
             :zoom-out (max 0.01 (- @zoom (* @zoom 0.1)))
             :zoom-reset 1
             @zoom))
          (b/merge shortcuts-zoom-stream
                   click-zoom-stream)))

(def tool-selected?-stream
  (b/map #(not= % :none)
         selected-tool-stream))
;; Atoms

(defonce scroll-top (b/to-atom top-scroll-stream))
(defonce scroll-left (b/to-atom left-scroll-stream))
(defonce selected-tool (b/to-atom selected-tool-stream))
(defonce zoom (b/to-atom (atom 1) zoom-stream))
(defonce grid? (b/to-atom (atom false) grid?-stream))
(defonce tool-selected? (b/to-atom (atom false) tool-selected?-stream))
