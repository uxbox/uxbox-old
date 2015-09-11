(ns uxbox.test.streams-test
  (:require [cljs.test :as t]
            [uxbox.streams :as s]))

(enable-console-print!)

;; helpers for testing

(defn drain!
  ([obs cb]
   (drain! obs cb #(println "Error: " %)))
  ([obs cb errb]
   (let [contents (volatile! [])]
     (s/on-value obs #(vswap! contents conj %))
     (s/on-error obs #(errb %))
     (s/on-end obs #(cb @contents)))))

(defn tick!
  [interval]
  (s/from-poll interval #(s/next (int (js/Date.)))))

;; Types

;; -property: reactive property, has the notion of current value (deref)
;;; .toProperty :: eventStream -> property
;; -event stream: stream of events, unsurprisingly
;; -bus: writable, streams can be plugged into it

;; Common event stream and property ops (core)

;; ::subscription
;; - subscribe
;; - onValue
;; - onValues
;; - onError
;; - onEnd

;; ::coercion
;; - toPromise
;; - firstToPromise

;; ::todo decomplect
;; - map
;; - mapError
;; - errors
;; - skipErrors
;; - mapEnd
;; - filter (pred, property)
;; - skipDuplicates
;; - take
;; - takeUntil
;; - takeWhile
;; - first
;; - last
;; - skip
;; - delay
;; - throttle
;; - debounce
;; - debounceImmediate
;; - bufferingThrottle
;; - doAction
;; - doError
;; - not
;; - flatMap
;; - flatMapLatest
;; - flatMapFirst
;; - flatMapError
;; - flatMapWithConcurrencyLimit
;; - flatMapConcat
;; - scan
;; - fold/reduce
;; - diff
;; - zip
;; - slidingWindow
;; - log
;; - doLog
;; - combine
;; - withStateMachine
;; - decode
;; - awaiting
;; - endOnError
;; - withHandler
;; - name
;; - withDescription

;; ::combination

;; - combineAsArray
;; - combineWith
;; - combineTemplate
;; - mergeAll
;; - zipAsArray
;; - zipWith
;; - onValues

;; tocino.events
;; event streams

;; ::todo creation

;; ::ops
;; - concat
;; - merge
;; - holdWhen
;; - startWith
;; - skipWhile
;; - skipUntil
;; - bufferWithTime
;; - bufferWithTimeOrCount
;; - toProperty

;; tocino.properties

(t/deftest properties
  (t/testing "`constant` creates a constant property"
    (t/async done
      (let [p (s/constant 42)]
        (t/is (s/property? p))
        (s/on-value p #(do (t/is (= % 42))
                           (done))))))

  (t/testing "`sample` samples the property every x miliseconds"
    (t/async done
      (let [ep (s/sample 1000 (s/constant 42))]
        (t/is (s/event-stream? ep))
        (s/on-value ep #(do
                          (t/is (= % 42))
                          (done))))))

  (t/testing "`sampled-by` samples a property on stream events"
    (t/async done
      (let [clock (s/to-property (tick! 10))
            ep (s/sampled-by clock (tick! 2))
            epsamples (s/take 2 ep)
            ep2 (s/sampled-by clock (tick! 20))
            ep2samples (s/take 2 ep2)]
        (t/is (s/property? clock))
        (t/is (s/event-stream? ep))
        (t/is (s/event-stream? ep2))

        (drain! epsamples (fn [[x y]]
                            (t/is (= x y))))
        (drain! ep2samples (fn [[x y]]
                             (t/is (not= x y))))
        (s/on-end (s/zip epsamples ep2samples) #(done)))))

  (t/testing "`sampled-by` accepts a combining function"
    (t/async done
      (let [clock (s/to-property (tick! 10))
            same-time? (fn [t1 t2]
                         (if (= t1 t2)
                           :same
                           :different))
            ep (s/sampled-by clock
                             clock
                             same-time?)
            epsamples (s/take 2 ep)
            ep2 (s/sampled-by clock
                              (tick! 20)
                              same-time?)
            ep2samples (s/take 2 ep2)]
        (drain! epsamples
                #(t/is (= % [:same :same])))
        (drain! ep2samples
                #(t/is (= % [:different :different])))
        (s/on-end (s/zip epsamples ep2samples) #(done)))))

  (t/testing "`changes` returns a stream of property changes"
    (t/async done
      (let [p (s/constant 42)
            sp (s/to-property (s/sample 10 p))
            cp (s/changes sp)
            cs (s/take 2 cp)]
        (t/is (s/event-stream? cp))
        (drain! cs
                #(t/is (= % [42 42])))
        (s/on-end cs done))))

  (t/testing "`and` combines properties with the boolean and operator"
    (t/async done
      (let [t  (s/constant true)
            f  (s/not t)

            tf (s/and t f)
            tt (s/and t t)
            ft (s/and f t)
            ff (s/and f f)

            ttt (s/and t t t)
            ttf (s/and t t f)]

        (s/on-value tt #(t/is (true? %)))
        (s/on-value tf #(t/is (false? %)))
        (s/on-value ft #(t/is (false? %)))
        (s/on-value ff #(t/is (false? %)))

        (s/on-value ttt #(t/is (true? %)))
        (s/on-value ttf #(do
                          (t/is (false? %))
                          (done))))))

  (t/testing "`or` combines properties with the boolean or operator"
    (t/async done
      (let [t  (s/constant true)
            f  (s/not t)

            tf (s/or t f)
            tt (s/or t t)
            ft (s/or f t)
            ff (s/or f f)

            fff (s/or f f f)
            fft (s/or f f t)]

        (s/on-value tt #(t/is (true? %)))
        (s/on-value tf #(t/is (true? %)))
        (s/on-value ft #(t/is (true? %)))
        (s/on-value ff #(t/is (false? %)))

        (s/on-value fff #(t/is (false? %)))
        (s/on-value fft #(do
                          (t/is (true? %))
                          (done)))))))

;; tocino.bus
;; TODO
