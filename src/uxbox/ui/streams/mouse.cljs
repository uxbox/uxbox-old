(ns uxbox.ui.streams.mouse
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

(defn coords-delta
  [[old new]]
  (let [[oldx oldy] old
        [newx newy] new]
    [(* 2 (- newx oldx))
     (* 2 (- newy oldy))]))

(def ^{:doc "A stream of mouse coordinate deltas as `[dx dy]` vectors."}
  delta
  (s/map coords-delta (s/partition 2 client-position)))
