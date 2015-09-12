(ns uxbox.test.streams-test
  (:require [cljs.test :as t]
            [cats.core :as m]
            [uxbox.streams :as s]))

(enable-console-print!)

;; --- helpers for testing

(def no-op (fn [& args]))

(defmacro with-timeout [ms & body]
  `(js/setTimeout
    (fn []
      (do
        ~@body))
    ~ms))

(defn drain!
  ([obs cb]
   (drain! obs cb #(println "Error: " %)))
  ([obs cb errb]
   (let [values (volatile! [])]
     (s/on-value obs #(vswap! values conj %))
     (s/on-error obs #(errb %))
     (s/on-end obs #(cb @values)))))

(defn tick
  [interval]
  (s/from-poll interval #(s/next (int (js/Date.)))))

;; ---

;; event stream

(t/deftest event-stream-from-vector
  (t/async done
    (let [coll [1 2 3]
          s (s/from-coll coll)]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % coll)))
      (s/on-end s done))))

(t/deftest event-stream-from-set
  (t/async done
    (let [coll #{1 2 3}
          s (s/from-coll coll)]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= (set %) coll)))
      (s/on-end s done))))

(t/deftest event-stream-from-callback
  (t/async done
    (let [s (s/from-callback (fn [sink]
                               (with-timeout 10
                                 (sink 1))))]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [1])))
      (s/on-end s done))))

(t/deftest event-stream-from-binder
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (with-timeout 10
                               (sink (s/next 1))
                               (sink (s/next 2))
                               (sink (s/next 3))
                               (sink (s/end)))
                             no-op))]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [1 2 3])))
      (s/on-end s done))))

(t/deftest event-stream-errors-from-binder
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (with-timeout 10
                               (sink (s/next 1))
                               (sink (s/error "oh noes"))
                               (sink (s/end)))
                             no-op))]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [1]))
                     #(t/is (= % "oh noes")))
             (s/on-end s done))))

(t/deftest event-stream-no-more-from-binder
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (with-timeout 10
                               (sink (s/next 1))
                               (sink s/no-more)
                               (sink (s/end)))
                             no-op))]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [1 s/no-more])))
      (s/on-end s done))))

(t/deftest event-stream-more-from-binder
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (with-timeout 10
                               (sink (s/next 1))
                               (sink s/more)
                               (with-timeout 3
                                 (sink 2)
                                 (sink (s/end))))
                             no-op))]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [1 s/more 2])))
      (s/on-end s done))))

(t/deftest event-stream-repeat
  (t/async done
    (let [s (s/repeat (fn [i]
                        (when (< i 3)
                          (s/once i))))]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [0 1 2])))
      (s/on-end s done))))

(t/deftest event-stream-once
  (t/async done
    (let [s (s/once 1)]
      (t/is (s/event-stream? s))
      (drain! s #(t/is (= % [1])))
      (s/on-end s done))))

(t/deftest event-stream-never
  (t/async done
    (let [n (s/never)]
      (s/on-end n done))))

(t/deftest event-stream-interval
  (t/async done
    (let [i (s/interval 10)
          sample (s/take 4 i)]
      (drain! sample #(t/is (apply = %)))
      (s/on-end sample done))))

(t/deftest event-stream-later
  (t/async done
    (let [s (s/later 10 42)]
      (drain! s #(t/is (= % [42])))
      (s/on-end s done))))

(t/deftest event-stream-sequentially
  (t/async done
    (let [s (s/sequentially 10 [1 2 3])
          sample (s/take 6 s)]
      (drain! sample #(t/is (= % [1 2 3])))
      (s/on-end sample done))))

(t/deftest event-stream-repeatedly
  (t/async done
    (let [s (s/repeatedly 10 [1 2 3])
          sample (s/take 6 s)]
      (drain! sample #(t/is (= % [1 2 3 1 2 3])))
      (s/on-end sample done))))

(t/deftest event-stream-on-value
  (t/async done
    (let [s (s/from-coll [1 2 3])
          vacc (volatile! [])]
      (s/on-value s #(vswap! vacc conj %))
      (s/on-end s #(do (t/is (= @vacc [1 2 3]))
                       (done))))))

(t/deftest event-stream-on-error
  (t/async done
    (let [s (s/from-coll [1 2 3 (s/error :oh) (s/error :noes)])
          vacc (volatile! [])]
      (s/on-error s #(vswap! vacc conj %))
      (s/on-end s #(do (t/is (= @vacc [:oh :noes]))
                       (done))))))

(t/deftest event-stream-initial
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (sink (s/initial 1))))]
      (s/subscribe s (fn [ev]
                       (t/is (s/initial? ev))
                       (t/is (= @ev 1))
                       (done))))))

(t/deftest event-stream-next
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (sink (s/next 1))))]
      (s/subscribe s (fn [ev]
                       (t/is (s/next? ev))
                       (t/is (= @ev 1))
                       (done))))))

(t/deftest event-stream-error
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (sink (s/error :oh-no))))]
      (s/subscribe s (fn [ev]
                       (t/is (s/error? ev))
                       (t/is (= @ev :oh-no))
                       (done))))))

(t/deftest event-stream-end
  (t/async done
    (let [s (s/from-binder (fn [sink]
                             (sink (s/end))
                             no-op))]
      (s/subscribe s (fn [ev]
                       (t/is (s/end? ev))
                       (done))))))

;; - concat
;; - merge
;; - holdWhen
;; - startWith
;; - skipWhile
;; - skipUntil
;; - bufferWithTime
;; - bufferWithTimeOrCount
;; - toProperty

;; property

(t/deftest property-constant
  (t/async done
    (let [p (s/constant 42)]
      (t/is (s/property? p))
      (s/on-value p #(do (t/is (= % 42))
                         (done))))))

(t/deftest property-sample
  (t/async done
    (let [ep (s/sample 1000 (s/constant 42))]
      (t/is (s/event-stream? ep))
      (s/on-value (s/take 1 ep) #(do
                                   (t/is (= % 42))
                                   (done))))))

(t/deftest property-sampled-by
  (t/async done
    (let [clock (s/to-property (tick 10))
          ep (s/sampled-by clock (tick 2))
          epsamples (s/take 2 ep)
          ep2 (s/sampled-by clock (tick 20))
          ep2samples (s/take 2 ep2)]
      (t/is (s/property? clock))
      (t/is (s/event-stream? ep))
      (t/is (s/event-stream? ep2))

      (drain! epsamples (fn [[x y]]
                          (t/is (= x y))))
      (drain! ep2samples (fn [[x y]]
                           (t/is (not= x y))))

      (s/on-end (s/zip epsamples ep2samples)
                done))))

(t/deftest property-sampled-by-combining-function
  (t/async done
    (let [clock (s/to-property (tick 10))
          same-time? (fn [t1 t2]
                       (if (= t1 t2)
                         :same
                         :different))
          ep (s/sampled-by clock
                           clock
                           same-time?)
          epsamples (s/take 2 ep)
          ep2 (s/sampled-by clock
                            (tick 20)
                            same-time?)
          ep2samples (s/take 2 ep2)]
      (drain! epsamples
              #(t/is (= % [:same :same])))
      (drain! ep2samples
              #(t/is (= % [:different :different])))
      (s/on-end (s/zip epsamples ep2samples) #(done)))))

(t/deftest property-changes
  (t/async done
    (let [p (s/constant 42)
          sp (s/to-property (s/sample 10 p))
          cp (s/changes sp)
          cs (s/take 2 cp)]
      (t/is (s/event-stream? cp))
      (drain! cs
              #(t/is (= % [42 42])))
      (s/on-end cs done))))

(t/deftest property-and
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

(t/deftest property-or
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
                        (done))))))

;; bus

(t/deftest bus-push
  (t/async done
    (let [b (s/bus)]
      (t/is (s/bus? b))
      (drain! b #(t/is (= % [1 2 3])))
      (s/push! b 1)
      (s/push! b 2)
      (s/push! b 3)
      (s/end! b)
      (s/on-end b done))))

(t/deftest bus-erro
  (t/async done
    (let [b (s/bus)]
      (t/is (s/bus? b))
      (drain! b #(t/is (= % [1]))
                #(t/is (= % :oh-no)))
      (s/push! b 1)
      (s/error! b :oh-no)
      (s/end! b)
      (s/on-end b done))))

(t/deftest bus-plug
  (t/async done
    (let [b (s/bus)]
      (t/is (s/bus? b))
      (s/plug! b (s/from-coll [1 2 3]))
      (s/plug! b (s/from-coll [:four :five]))
      (drain! b #(t/is (= %
                          [1 2 3 :four :five])))
      (s/end! b)
      (s/on-end b done))))

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

;; TODO

;; - combineAsArray
;; - combineWith
;; - combineTemplate
;; - mergeAll
;; - zipAsArray
;; - zipWith
;; - onValues


;; TODO: error handling
;; - onError, ...
;; - retry

;; TODO: join
;; - when

(t/deftest property-as-functor
  (t/async done
    (let [p (s/constant 41)
          life (m/fmap inc p)]
      (t/is (s/property? life))
      (s/on-value life #(do (t/is (= % 42))
                            (done))))))

(t/deftest property-as-applicative
  (t/async done
    (let [pinc (m/pure s/property-context inc)
          pval (m/pure s/property-context 41)
          life (m/fapply pinc pval)]
      (t/is (s/property? life))
      (s/on-value life #(do (t/is (= % 42))
                            (done))))))

(t/deftest property-as-monad
  (t/async done
    (let [pn (s/to-property (s/from-coll [1 2 3]))
          pnks (m/mlet [n pn
                        k (s/from-coll (map (comp keyword str) (range 1 (inc n))))]
                 (m/return [n k]))
          sample (s/take 6 pnks)]
      (t/is (s/property? pnks))
      (drain! sample #(t/is (= % [[1 :1]
                                  [2 :1]
                                  [2 :2]
                                  [3 :1]
                                  [3 :2]
                                  [3 :3]])))
      (s/on-end sample done))))

(t/deftest event-stream-as-functor
 (t/async done
   (let [s (s/from-coll [0 1 2])
         s2 (m/fmap inc s)]
     (t/is (s/event-stream? s))
     (t/is (s/event-stream? s2))
     (drain! s2 #(do (t/is (= % [1 2 3]))
                      (done))))))

(t/deftest event-stream-as-applicative
 (t/async done
   (let [pinc (m/pure s/event-stream-context inc)
         pval (m/pure s/event-stream-context 41)
         life (m/fapply pinc pval)]
     (t/is (s/event-stream? life))
     (drain! life #(do (t/is (= % [42]))
                       (done))))))

(t/deftest event-stream-as-monad
  (t/async done
    (let [sn (s/from-coll [1 2 3])
          snks (m/mlet [n sn
                        k (s/from-coll (map (comp keyword str) (range 1 (inc n))))]
                 (m/return [n k]))
          sample (s/take 6 snks)]
      (t/is (s/event-stream? snks))
      (drain! sample #(t/is (= % [[1 :1]
                                  [2 :1]
                                  [2 :2]
                                  [3 :1]
                                  [3 :2]
                                  [3 :3]])))
      (s/on-end sample done))))

(t/deftest bus-functor
 (t/async done
   (let [b (s/bus)
         b2 (m/fmap inc b)]
     (t/is (s/bus? b))
     (t/is (s/bus? b2))
     (drain! b2 #(do (t/is (= % [1 2 3]))
                     (done)))
     (s/push! b 0)
     (s/push! b 1)
     (s/push! b 2)
     (s/end! b))))
