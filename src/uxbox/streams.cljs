(ns uxbox.streams
  (:require [bacon])
  (:refer-clojure :exclude [map filter reduce zip dedupe drop take not and next]))

;; core

(defn take
  [n obs]
  (.take obs n))

(defn map
  [f obs]
  (.map obs f))

(defn not
  [obs]
  (.not obs))

(defn start-with
  [value obs]
  (.startWith obs value))

;; combination

(defn zip
  ([o1 o2]
   (.zip  o1 o2 vector))
  ([zf o1 o2]
   (.zip o1 o2 (comp zf vector))))

;; creation

;; Fn -> Observable
(defn from-callback
  [cb]
  (js/Bacon.fromCallback cb))

;; interval -> Fn -> Observable
(defn from-poll
  [interval pf]
  (js/Bacon.fromPoll interval pf))

;; Observable -> Fn -> ()
(defn on-value
  [obs f]
  (.onValue obs f))

;; Observable -> Fn -> ()
(defn on-error
  [obs f]
  (.onError obs f))

;; Observable -> Fn -> ()
(defn on-end
  [obs f]
  (.onEnd obs f))

;;; transformations

#_(defn transform
  [stream xform]
  (let [ns (js/Bacon.fromBinder (fn [sink]
                                  (let [step (xform (fn [_ input]
                                                      ;; TODO: completion step, respect reduced
                                                      (sink input)
                                                      input))
                                        unsub (.onValue stream #(step nil %))]
                                    (fn []
                                      (unsub)))))]
    ns))

;; debugging

#_(defn log
  ([stream]
   (.log stream)
   stream)
  ([logger stream]
   (.log (map logger stream))
   stream))

#_(defn pr-log
  ([stream]
   (log #(pr-str %) stream)
   stream)
  ([prefix stream]
   (log #(pr-str prefix %) stream)
   stream))

;; Types

;;
;; tocino.event-stream

;; any -> boolean
(defn event-stream?
  [s]
  (instance? js/Bacon.EventStream s))

;; low-level

(defn next
  [v]
  (js/Bacon.Next. v))

(defn error
  [e]
  (js/Bacon.Error. e))

(defn initial
  [v]
  (js/Bacon.Initial. v))

(defn end
  []
  (js/Bacon.End.))

;;
;; tocino.property

;; any -> boolean
(defn property?
  [p]
  (instance? js/Bacon.Property p))

;; a -> property a
(defn constant
  [v]
  (js/Bacon.constant v))

;; observable -> property
(defn to-property
  [obs]
  (.toProperty obs))

;; miliseconds -> property -> event-stream
(defn sample
  [milis p]
  (.sample p milis))

;; property -> observable -> (maybe Fn) -> stream
(defn sampled-by
  ([p obs]
   (.sampledBy p obs))
  ([p obs cf]
   (.sampledBy p obs cf)))

;; property -> event-stream
(defn changes
  [p]
  {:pre [(property? p)]}
  (.changes p))

(defn and
  ([p1 p2]
   {:pre [(property? p1)
          (property? p2)]}
   (.and p1 p2))
  ([p1 p2 & ps]
   (cljs.core/reduce and
                     (and p1 p2)
                     ps)))

(defn or
  ([p1 p2]
   {:pre [(property? p1)
          (property? p2)]}
   (.or p1 p2))
  ([p1 p2 & ps]
   (cljs.core/reduce or
                     (or p1 p2)
                     ps)))

;;
;; tocino.bus

;; any -> boolean
(defn bus?
  [b]
  (instance? js/Bacon.Bus b))
