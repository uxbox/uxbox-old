(ns uxbox.impl.routing-spec
  (:refer-clojure :exclude [send])
  (:require [clojure.test :as t]
            [clojure.core.async :as a]
            [catacumba.core :as ct]
            [catacumba.serializers :as sz]
            [catacumba.testing :refer [with-server]]
            [uxbox.impl.routing :as rt]
            [promissum.core :as p]
            [aleph.http :as http]
            [clj-uuid :as uuid]
            [manifold.deferred :as d]
            [manifold.stream :as s]))

(defn timeout
  [ms]
  (p/future
    (Thread/sleep ms)
    nil))

(defn recv
  [conn]
  (rt/decode @(s/take! conn)))

(defn send
  [conn data]
  @(s/put! conn (rt/encode data)))

(defn random-uuid
  []
  (uuid/v1))

(defn- do-handshake
  [conn]
  (send conn {:cmd :hello})
  (let [rsp (recv conn)]
    (t/is (= {:cmd :hello} rsp))))

(t/deftest simple-query
  (let [p (promise)]
    (letfn [(handler [context frame]
              (deliver p frame)
              (rt/response {:ok true}))]
      (with-server {:handler (rt/router handler)}
        (let [conn @(http/websocket-client "ws://localhost:5050/")
              uuid (random-uuid)]
          (do-handshake conn)
          (send conn {:cmd :query :id uuid :data {}})
          (let [response (recv conn)]
            (t/is (= (:cmd response) :response))
            (t/is (= (:id response) uuid)))
          (let [result (deref p 1000 nil)]
            (t/is (= (:cmd result) :query))
            (t/is (= (:id result) uuid)))
          (s/close! conn))))))

(t/deftest on-close-handlers-1
  (let [p (promise)]
    (letfn [(handler [context frame]
              (rt/on-close context #(deliver p 1))
              (rt/response {:ok true}))]
      (with-server {:handler (rt/router handler)}
        (let [conn @(http/websocket-client "ws://localhost:5050/")]
          (do-handshake conn)
          (send conn {:cmd :query :id (random-uuid) :data {}})
          (recv conn)
          (s/close! conn)

          (t/is (= 1 (deref p 1000 nil))))))))

(t/deftest on-close-handlers-2
  (let [p (promise)]
    (letfn [(handler [context frame]
              (rt/on-close context #(deliver p 1))
              (future
                (Thread/sleep 300)
                (a/close! (:out context)))
              (rt/response {:ok true}))]
      (with-server {:handler (rt/router handler)}
        (let [conn @(http/websocket-client "ws://localhost:5050/")]
          (do-handshake conn)
          (send conn {:cmd :query :id (random-uuid) :data {}})
          (recv conn)
          (t/is (= 1 (deref p 1000 nil))))))))


(t/deftest state-persists-in-same-session
  (let [p (promise)]
    (letfn [(handler [{:keys [state] :as context} frame]
              (swap! state update :counter (fnil inc 0))
              (rt/response @state))]
      (with-server {:handler (rt/router handler)}
        (let [conn @(http/websocket-client "ws://localhost:5050/")]
          (do-handshake conn)
          (send conn {:cmd :query :id (random-uuid) :data {}})
          (let [response (recv conn)]
            (t/is (= (:cmd response) :response))
            (t/is (= (:data response) {:counter 1})))

          (send conn {:cmd :query :id (random-uuid) :data {}})
          (let [response (recv conn)]
            (t/is (= (:cmd response) :response))
            (t/is (= (:data response) {:counter 2}))))))))

