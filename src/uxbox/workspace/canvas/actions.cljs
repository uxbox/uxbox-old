(ns uxbox.workspace.canvas.actions
  (:require [uxbox.data.projects :as dp]
            [uxbox.log.core :as log]))

(defn draw-shape
  [page shape]
  (let [s (dp/create-shape (random-uuid) (:page/uuid page) shape)]
    (log/record! :uxbox/create-shape s)))

(defn update-shapes
  [shapes]
  (log/record! :uxbox/update-shapes shapes))

(defn change-shape
  [shape attr val]
  (log/record! :uxbox/change-shape [shape attr val]))

(defn toggle-shape-visbility
  [shape]
  (log/record! :uxbox/change-shape-visibility shape))

(defn toggle-shape-lock
  [shape]
  (log/record! :uxbox/change-shape-lock shape))
