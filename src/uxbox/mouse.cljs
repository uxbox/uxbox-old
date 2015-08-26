(ns uxbox.mouse
  (:require
   [goog.events :as events]
   [cljs.core.async :as async]
   [jamesmacaulay.zelkova.signal :as z])
  (:import [goog.events EventType]))

(defn- listen
  [el type & args]
  (let [out (apply async/chan 1 args)]
    (events/listen el type (fn [e] (async/put! out e)))
    out))

(defn- client-position-channel
  [graph opts]
  (listen js/document
          EventType.MOUSEMOVE
          (map (fn [e]
                 [(.-clientX (.-event_ e))
                  (.-clientY (.-event_ e))]))))

(def ^{:doc "A signal of client mouse coordinates as `[x y]` vectors. Initial value is `[0 0]`."}
  client-position
  (z/input [0 0] ::client-position client-position-channel))

(def delta
  (->> (z/reductions (fn [[_ old] new]
                      [old new])
                    [[0 0] [0 0]]
                    client-position)
       (z/map (fn [[[oldx oldy] [newx newy]]]
                [(- newx oldx) (- newy oldy)]))))
