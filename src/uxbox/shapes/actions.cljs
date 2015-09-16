(ns uxbox.shapes.actions
  (:require [uxbox.shapes.data :as d]
            [uxbox.log.core :as log]))

(defn draw-shape
  [conn page shape]
  (let [s (d/create-shape (random-uuid) (:page/uuid page) shape)]
    (log/record! conn :uxbox/create-shape s)))

(defn update-shapes
  [conn shapes]
  (log/record! conn :uxbox/update-shapes shapes))

(defn change-shape
  [conn shape attr val]
  (log/record! conn :uxbox/change-shape [shape attr val]))

(defn toggle-shape-visibility
  [conn shape]
  (log/record! conn :uxbox/toggle-shape-visibility shape))

(defn toggle-shape-lock
  [conn shape]
  (log/record! conn :uxbox/toggle-shape-lock shape))
