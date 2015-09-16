(ns uxbox.ui.canvas
  (:require
   rum
   [uxbox.streams :as s]
   [uxbox.ui.streams.mouse :as mouse]
   [uxbox.ui.keyboard :as k]
   [uxbox.shapes.actions :as actions]
   [cljs.core.async :as async]
   [uxbox.ui.canvas.streams :as cs]
   [uxbox.geometry :as geo]
   [cuerdas.core :as str]
   [uxbox.shapes.protocols :as shapes]))

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

(def canvas-coordinates (s/pipe-to-atom cs/canvas-coordinates-signal))
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

(def draw! (s/pipe-to-atom cs/draw-signal))
(def move! (s/pipe-to-atom cs/move-signal))
(def drawing (s/pipe-to-atom cs/draw-in-progress))
(def selected-shapes (s/pipe-to-atom (s/map vals cs/selected)))
(def selected-ids (s/pipe-to-atom (s/map (comp set keys) cs/selected)))

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
                       (cs/set-current-shapes! shapes)
                       new))})

(rum/defc canvas < rum/reactive
                   (cmds-mixin [::draw draw! (fn [[conn page] shape]
                                               (actions/draw-shape conn page shape))]
                               [::move move! (fn [[conn _ shapes] selections]
                                               (actions/update-shapes conn selections))])
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
        raw-shapes (into []
                         (comp
                          (filter :shape/visible?)
                          (filter #(not (contains? selection-uuids (:shape/uuid %))))
                          (map :shape/data))
                         shapes)]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height
      :on-mouse-down cs/on-mouse-down
      :on-mouse-up cs/on-mouse-up}
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
