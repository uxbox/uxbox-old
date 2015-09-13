(ns uxbox.mouse
  (:require
   [uxbox.streams :as s])
  (:import [goog.events EventType]))

(def ^{:doc "A stream of client mouse coordinates as `[x y]` vectors."}
  client-position
  (s/dedupe (s/from-event js/document
                          EventType.MOUSEMOVE
                          (fn [e]
                            [(.-clientX e)
                             (.-clientY e)]))))

;; [[x y], [x y]] -> [dx dy]
(defn coords-delta
  [[old new]]
  (let [[oldx oldy] old
        [newx newy] new]
    [(- newx oldx) (- newy oldy)]))

(def ^{:doc "A stream of mouse coordinate deltas as `[dx dy]` vectors."}
  delta client-position #_(s/map
           coords-delta
           (s/zip client-position (s/drop 1 client-position))))
