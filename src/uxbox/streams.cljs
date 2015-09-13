(ns uxbox.streams
  (:require [bacon]
            [cats.protocols :as p]
            [cats.context :as ctx])
  (:refer-clojure :exclude [map
                            filter
                            reduce
                            merge
                            repeat
                            repeatedly
                            zip
                            dedupe
                            drop
                            take
                            not
                            and
                            or
                            next
                            concat]))

;; core

(defn flat-map
  [obs f]
  (.flatMap obs f))

;;;;; coercions

;; observable -> property
(defn to-property
  [obs]
  (.toProperty obs))

(defn to-event-stream
  [p]
  (.toEventStream p))

(defn to-promise
  ([obs]
   (.toPromise obs))
  ([obs ctr]
   (.toPromise obs ctr)))

(defn first-to-promise
  ([obs]
   (.firstToPromise obs))
  ([obs ctr]
   (.firstToPromise obs ctr)))

;;;;; transformations

(defn reduce
  [rf seed obs]
  (.reduce obs seed rf))

(defn take
  [n obs]
  (.take obs n))

(defn map
  [f obs]
  (.map obs f))

(defn filter
  [pred obs]
  (.filter obs pred))

(defn dedupe
  [obs]
  (.skipDuplicates obs))

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

;; subscription

(defn on-value
  [obs f]
  (.onValue obs f))

(defn on-error
  [obs f]
  (.onError obs f))

(defn on-end
  [obs f]
  (.onEnd obs f))

(defn subscribe
  [obs sf]
  (.subscribe obs sf))

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

;; creation

(defn once
  [v]
  (js/Bacon.once v))

(defn never
  []
  (js/Bacon.never))

(defn repeat
  [rf]
  (js/Bacon.repeat rf))

(defn interval
  ([ms]
   (js/Bacon.interval ms))
  ([ms v]
   (js/Bacon.interval ms v)))

(defn later
  [ms v]
   (js/Bacon.later ms v))

(defn sequentially
  [ms coll]
  (js/Bacon.sequentially ms (into-array coll)))

(defn repeatedly
  [ms coll]
  (js/Bacon.repeatedly ms (into-array coll)))

(defn from-coll
  [coll]
  (js/Bacon.fromArray (into-array coll)))

(defn from-callback
  [cb]
  (js/Bacon.fromCallback cb))

(defn from-poll
  [interval pf]
  (js/Bacon.fromPoll interval pf))

(defn from-binder
  [bf]
  (js/Bacon.fromBinder bf))

(defn initial
  [v]
  (js/Bacon.Initial. v))

(defn initial?
  [i]
  (instance? js/Bacon.Initial i))

(extend-type js/Bacon.Initial
  IDeref
  (-deref [ev]
    (.-valueInternal ev)))

(defn next
  [v]
  (js/Bacon.Next. v))

(defn next?
  [v]
  (instance? js/Bacon.Next v))

(extend-type js/Bacon.Next
  IDeref
  (-deref [ev]
    (.value ev)))

(defn error
  [e]
  (js/Bacon.Error. e))

(extend-type js/Bacon.Error
  IDeref
  (-deref [err]
    (.-error err)))

(defn error?
  [e]
  (instance? js/Bacon.Error e))

(defn end
  []
  (js/Bacon.End.))

(defn end?
  [e]
  (instance? js/Bacon.End e))

(defn has-value?
  [ev]
  (.hasValue ev))

(def more js/Bacon.more)
(def no-more js/Bacon.noMore)

;; ops

(defn concat
  ([one other]
   (.concat one other))
  ([one other & others]
   (cljs.core/reduce concat
                     (.concat one other)
                     others)))

(defn merge
  ([one other]
   (.merge one other))
  ([one other & others]
   (cljs.core/reduce merge
                     (.merge one other)
                     others)))

(defn hold-when
  [stream valve]
  (.holdWhen stream valve))

(defn skip-while
  {:pre [(or (property? p)
             (fn? p))]}
  [stream p]
  (.skipWhile stream p))

(defn skip-until
  {:pre [(event-stream? s)]}
  [stream s]
  (.skipUntil stream s))

;; cats integration

(def event-stream-context
  (reify
    p/Context
    (-get-level [_] ctx/+level-default+)

    p/Functor
    (-fmap [_ f obs]
      (.map obs f))

    p/Applicative
    (-pure [_ v]
      (once v))

    (-fapply [_ pf pv]
      (js/Bacon.zipWith #(%1 %2) pf pv))

    p/Monad
    (-mreturn [_ v]
      (once v))

    (-mbind [_ mv f]
      (.flatMap mv f))))

(extend-type js/Bacon.EventStream
  p/Contextual
  (-get-context [_] event-stream-context))

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

;; cats integration

(def property-context
  (reify
    p/Context
    (-get-level [_] ctx/+level-default+)

    p/Functor
    (-fmap [_ f obs]
      (.map obs f))

    p/Applicative
    (-pure [_ v]
      (constant v))

    (-fapply [_ pf pv]
      (to-property (js/Bacon.zipWith #(%1 %2) pf pv)))

    p/Monad
    (-mreturn [_ v]
      (constant v))

    (-mbind [_ p f]
      (to-property (.flatMap p f)))))

(extend-type js/Bacon.Property
  p/Contextual
  (-get-context [_] property-context))

;; tocino.bus

;; any -> boolean
(defn bus?
  [b]
  (instance? js/Bacon.Bus b))

(defn bus
  []
  (js/Bacon.Bus.))

(defn push!
  [b v]
  (.push b v))

(defn error!
  [b e]
  (.error b e))

(defn plug!
  [b obs]
  (.plug b obs))

(defn end!
  [b]
  (.end b))

(def bus-context
  (reify
    p/Context
    (-get-level [_] ctx/+level-default+)

    p/Functor
    (-fmap [_ f b]
      (let [nb (bus)]
        (on-value b #(push! nb (f %)))
        (on-error b #(error! nb %))
        (on-end b #(end! nb))
        nb))))

(extend-type js/Bacon.Bus
  p/Contextual
  (-get-context [_] bus-context))

;;

(defn observable?
  [o]
  (or (property? o)
      (event-stream? o)
      (bus? o)))
