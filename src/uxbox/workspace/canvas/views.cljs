(ns uxbox.workspace.canvas.views
  (:require
   rum
   [cats.core :as m]
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]
   [uxbox.keyboard :as k]
   [uxbox.data.log :refer [record]]
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

(defonce canvas-coordinates (s/pipe-to-atom signals/canvas-coordinates-signal))
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

(defn- sub-all
  [sigs args]
  (into {} (map (fn [[k signal eff]]
                  [k (s/on-value signal #(eff args %))])
                sigs)))

(defn- unsub-all
  [subs]
  (doseq [unsub (vals subs)]
    (unsub)))

(defn signals-mixin
  [& sigs]
  {:will-mount (fn [state]
                 (let [args (:rum/args state)
                       page (second args)
                       subs (sub-all sigs args)]
                   (merge state {::subs subs
                                 ::signals sigs})))
   :transfer-state (fn [old new]
                     (let [args (:rum/args new)
                           oldsubs (::subs old)
                           sigs (::signals old)]
                      (unsub-all oldsubs)
                      (merge new {::subs (sub-all sigs args)
                                  ::signals sigs})))
   :wrap-render (fn [render-fn]
                  (fn [state]
                    (let [[dom next-state] (render-fn state)]
                      [dom (merge next-state (select-keys state [::subs ::signals]))])))
   :will-unmount (fn [state]
                   (unsub-all (::subs state))
                   (-> (dissoc state ::subs)
                       (dissoc state ::signals)))})

(def canvas-mixin (signals-mixin [::draw signals/draw-signal (fn [[_ page] shape]
                                                               (actions/draw-shape page shape))]))

(defonce drawing (s/pipe-to-atom signals/draw-in-progress))

(rum/defc canvas < rum/reactive canvas-mixin
  [conn
   page
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:page/width page)
        page-height (:page/height page)
        raw-shapes (map :shape/data shapes)]
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
     #_(when-let [selected-shapes (get page :selected)]
       (map shapes/selected-svg selected-shapes)
       (shapes/shape->selected-svg (get shapes selected-uuid)))]))

;; mouse down -> selcted-tool? - no -> intersection? - yes -> select
;;                                                    - no -> deselect
;; toggle-selection-signal

;; mouse drag -> drawing? - no -> selected stuff? - yes -> move selections
;; mouse up -> drawing? - yes -> end drawing (implies selection of just drawn?)

;; shape-select :: mouse click -> (intersects with shape) -> (shape is not selected) -> selection shape
;; shape-deselect :: mouse click -> (doesn't intersect with shape) -> (shapes are selected) -> deselect all shapes
