(ns uxbox.workspace.canvas.views
  (:require
   rum
   [uxbox.data.log :refer [record]]
   [uxbox.workspace.tools :as tools]
   [uxbox.workspace.signals :as wsigs]
   [jamesmacaulay.zelkova.mouse :as zm]
   [jamesmacaulay.zelkova.signal :as z]
   [cljs.core.async :as async]
   [uxbox.workspace.canvas.actions :as actions]
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

(rum/defc debug-coordinates < rum/reactive
  []
  (let [[x y] (rum/react signals/canvas-coordinates)]
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

;; mouse down -> selected-tool? - yes -> start drawing! (implies deselect)

#_(def start-drawing-signal
  (z/sample-on signals/mouse-down
               (z/combine [signals/mouse-down
                           wsigs/selected-tool-signal])))

#_(def drawing-signal
  (z/map
   (fn [[coords tool]] (if (= tool :none)
                         :nodraw
                         (tools/start-drawing tool coords)))
    start-drawing-signal))

#_(def drawn-signal
  (z/sample-on signals/mouse-up
               drawing-signal))

#_(def drawing
  (z/pipe-to-atom drawing-signal))

#_(def drawn
  (z/pipe-to-atom drawn-signal))

#_(add-watch drawing
           :dr
           (fn [_ _ _ new-val]
             (println :drawing new-val)))
#_(add-watch drawn
           :dr
           (fn [_ _ _ new-val]
             (println :drawn new-val)))

;; mouse down -> selcted-tool? - no -> intersection? - yes -> select
;;                                                    - no -> deselect
;; toggle-selection-signal

;; mouse drag -> drawing? - yes -> update drawing!
;;
;;                        - no -> selected stuff? - yes -> move selections
;;                                                - no ->

;; mouse up -> drawing? - yes -> end drawing (implies selection of just drawn)

;; shape-select :: mouse click -> (intersects with shape) -> (shape is not selected) -> selection shape
;; shape-deselect :: mouse click -> (doesn't intersect with shape) -> (shapes are selected) -> deselect all shapes



(def canvas-init
  {:will-mount (fn [state]
                 (let [shape-state (:shape-state state)
                       conn (first (:rum/args state))]
                   ;; up
                   #_(go-loop [coords (<! ups)]
                     (when coords
                       (println :up coords)
                       (when (:drawing @shape-state)
                         (let [drawn (:drawing @shape-state)]
                           (swap! shape-state dissoc :drawing)
                           (record :uxbox/create-shape {:shape/uuid (random-uuid)
                                                        :shape/page (:page/uuid page)
                                                        :shape/data drawn
                                                        :shape/locked? false
                                                        :shape/visible? true})))


                       (recur (<! ups))))

                   #_(go-loop [draw (<! start-drawing)]
                     (when-not (nil? draw)
                       (let [[coords tool] draw]
                         (swap! shape-state
                                assoc
                                :drawing
                                (tools/start-drawing tool coords)))
                       (recur (<! start-drawing))))

                   #_(go-loop [coords (<! drags)]
                     (when coords
                       (when-let [drawing (:drawing @shape-state)]
                         (let [[dx dy] coords]
                           ;; TODO: clamp
                           (swap! shape-state
                                  update
                                  :drawing
                                  shapes/drag-delta
                                  dx
                                  dy)))

                       (when-not (:drawing @shape-state)
                         )

                       (recur (<! drags))))

                   #_(assoc state ::canvas-chans [ups downs drags])
                   state))

   :will-unmount (fn [state]
                   (doseq [c (::canvas-chans state)]
                     (async/close! c))
                   #_(wsigs/select-tool! :none)
                   (dissoc state ::canvas-chans))})

(rum/defc background < rum/static
  []
  [:rect
   {:x 0
    :y 0
    :width "100%"
    :height "100%"
    :fill "white"}])

(rum/defcs canvas < (rum/local {:selected {}}
                               :shape-state)
                    canvas-init
                    rum/reactive
  [{:keys [shape-state]}
   conn
   page
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:width page)
        page-height (:height page)
        raw-shapes (map :shape/data shapes)]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height
      :on-mouse-down signals/on-mouse-down
      :on-mouse-up signals/on-mouse-up
      :on-mouse-drag signals/on-mouse-drag}
     (background)
     (apply vector :svg#page-layout (map shapes/shape->svg raw-shapes))
     #_(when-let [shape (rum/react drawing)]
       (shapes/shape->drawing-svg shape))
     #_(when-let [selected-shapes (get page :selected)]
       (map shapes/selected-svg selected-shapes)
       (shapes/shape->selected-svg (get shapes selected-uuid)))]))
