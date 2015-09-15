(ns uxbox.workspace.canvas.views
  (:require
   rum
   [cats.core :as m]
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]
   [uxbox.keyboard :as k]
   [uxbox.workspace.tools :as tools]
   [uxbox.workspace.signals :as wsigs]
   [uxbox.workspace.canvas.actions :as actions]
   [cljs.core.async :as async]
   [uxbox.workspace.canvas.signals :as signals]
   [uxbox.geometry :as geo]
   [cuerdas.core :as str]
   [uxbox.shapes.protocols :as shapes]
   [uxbox.shapes.line :refer [new-line]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(rum/defc grid < rum/static
  [width height start-width start-height zoom]
  (let [padding (* 20 zoom)
        ticks-mod (/ 100 zoom)
        step-size (/ 10 zoom)

        vertical-ticks (range (- padding start-height) (- height start-height padding) step-size)
        horizontal-ticks (range (- padding start-width) (- width start-width padding) step-size)

        vertical-lines (fn
          [position value padding]
          (if (< (mod value ticks-mod) step-size)
             [:line {:key position
                     :y1 padding
                     :y2 width
                     :x1 position
                     :x2 position
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.75}]
             [:line {:key position
                     :y1 padding
                     :y2 width
                     :x1 position
                     :x2 position
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.25}]))

        horizontal-lines (fn
          [position value padding]
          (if (< (mod value ticks-mod) step-size)
             [:line {:key position
                     :y1 position
                     :y2 position
                     :x1 padding
                     :x2 height
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.75}]
             [:line {:key position
                     :y1 position
                     :y2 position
                     :x1 padding
                     :x2 height
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.25}]))]
    [:g.grid
     (map #(vertical-lines (+ %1 start-width) %1 padding) vertical-ticks)
     (map #(horizontal-lines (+ %1 start-height) %1 padding) horizontal-ticks)]))

(def canvas-coordinates (s/pipe-to-atom signals/canvas-coordinates-signal))
(rum/defc debug-coordinates < rum/reactive
  []
  (let [[x y] (rum/react canvas-coordinates)]
    [:div
     {:style #js {:position "absolute"
                  :left "80px"
                  :top "20px"}}
     [:table
      [:tr
       [:td "X:"]
       [:td x]]
      [:tr
       [:td "Y:"]
       [:td y]]]]))

(rum/defc background < rum/static
  []
  [:rect
   {:x 0
    :y 0
    :width "100%"
    :height "100%"
    :fill "white"}])

(def draw! (s/pipe-to-atom signals/draw-signal))
(def move! (s/pipe-to-atom signals/move-signal))
(def drawing (s/pipe-to-atom signals/draw-in-progress))
(def selected-shapes (s/pipe-to-atom (s/map vals signals/selected)))
(def selected-ids (s/pipe-to-atom (s/map (comp set keys) signals/selected)))

(defn- sub-all
  [cmds args]
  (let [subs (into [] (for [[key action eff :as cmd] cmds]
                        [key action]))]
    (doseq [[key action eff :as cmd] cmds]
      (add-watch action
                 key
                 (fn [_ _ _ v]
                   (eff args v))))
    subs))

(defn unsub-all
  [subs]
  (doseq [[skey action] subs]
    (remove-watch action skey)))

(defn cmds-mixin
  [& cmds]
  {:will-mount (fn [state]
                 (let [args (:rum/args state)
                       subs (sub-all cmds args)]
                   (assoc state ::cmds subs)))
   :transfer-state (fn [old new]
                     (let [args (:rum/args new)
                           [_ _ shapes] args
                           oldsubs (::cmds old)]
                       (unsub-all oldsubs)
                       (assoc new ::cmds (sub-all cmds args))))
   :wrap-render (fn [render-fn]
                  (fn [state]
                    (let [[dom next-state] (render-fn state)]
                      [dom (assoc next-state ::cmds (::cmds state))])))
   :will-unmount (fn [state]
                   (unsub-all (::cmds state))
                   (dissoc state ::cmds))})

(def shapes-push-mixin
  {:transfer-state (fn [old new]
                     (let [[_ _ shapes] (:rum/args new)]
                       (signals/set-current-shapes! shapes)
                       new))})

(rum/defc canvas < rum/reactive
                   (cmds-mixin [::draw draw! (fn [[_ page] shape]
                                               (actions/draw-shape page shape))]
                               [::move move! (fn [[_ _ shapes] selections]
                                               (actions/update-shapes selections))])
                   shapes-push-mixin
  [conn
   page
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:page/width page)
        page-height (:page/height page)
        selection-uuids (rum/react selected-ids)
        selected-shapes (rum/react selected-shapes)
        shapes-to-draw (filter :shape/visible? shapes)
        raw-shapes (map :shape/data (filter #(not (contains? selection-uuids (:shape/uuid %))) shapes-to-draw))]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height
      :on-mouse-down signals/on-mouse-down
      :on-mouse-up signals/on-mouse-up}
     (background)
     (apply vector :svg#page-layout (map shapes/shape->svg raw-shapes))
     (when-let [shape (rum/react drawing)]
       (shapes/shape->drawing-svg shape))
     (when-not (empty? selected-shapes)
       (let [rs selected-shapes]
         (vec (cons :g
                    (concat
                     (map shapes/shape->selected-svg rs)
                     (map shapes/shape->svg rs))))))]))
