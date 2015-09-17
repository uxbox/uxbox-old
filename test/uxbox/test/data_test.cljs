(ns uxbox.test.data-test
  (:require
   [cljs.test :as t]
   [datascript :as d]
   [uxbox.data.db :as db]))

(t/deftest persistence
  (t/testing "DB can be persisted to and restored from a transient map"
    (let [conn (db/create)
          storage (transient {})]

      (db/persist-to! conn storage)
      (d/transact! conn [{:foo :bar}])

      (t/is :bar (first (d/q '[:find [?v]
                               :where [?e :foo ?v]]
                             @conn)))

      (let [nconn (db/create)]
        (db/restore! nconn storage)
        (t/is :bar (first (d/q '[:find [?v]
                               :where [?e :foo ?v]]
                             @nconn)))))))
