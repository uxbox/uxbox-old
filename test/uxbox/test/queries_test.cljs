(ns uxbox.test.queries-test
  (:require
   [cljs.test :as t]
   [datascript :as d]
   [uxbox.streams :as s]
   [uxbox.queries :as qs]
   [uxbox.data.db :as db]))

(t/deftest reactive-query
  (t/testing "Reactive queries can be constructed"
    (let [conn (db/create)
          a (qs/rquery '[:find [?n ...]
                         :where
                         [?e :name ?n]
                         [?e :cool? true]]
                        conn)]

      (d/transact! conn [{:name "John"
                          :cool? true}])
      (t/is (= @a
               ["John"]))

      (d/transact! conn [{:name "Mariano"
                          :cool? false}])
      (t/is (= @a
               ["John"]))

      (d/transact! conn [{:db/id 42
                          :name "Grace"
                          :cool? true}
                         {:name "Ada"
                          :cool? true}])

      (t/is (= @a
               ["John" "Grace" "Ada"]))

      (d/transact! conn [[:db/add 42 :cool? false]])

      (t/is (= @a
               ["John" "Ada"])))))

(t/deftest reactive-pull-query
  (t/testing "Reactive pull queries can be constructed"
    (let [conn (db/create)
          query '[:find [?e ...]
                  :where [?e :name ?n]
                         [?e :cool? true]]
          pull '[:name]
          a (qs/rpull query pull conn)]
      (d/transact! conn [{:name "John"
                          :cool? true}])
      (t/is (= @a
               [{:name "John"}]))

      (d/transact! conn [{:name "Mariano"
                          :cool? false}])
      (t/is (= @a
               [{:name "John"}]))

      (d/transact! conn [{:db/id 42
                          :name "Ada"
                          :cool? true}])

      (t/is (= @a
               [{:name "John"}
                {:name "Ada"}]))

      (d/transact! conn [[:db/add 42 :name "Ada Lovelace"]])

      (t/is (= @a
               [{:name "John"}
                {:name "Ada Lovelace"}])))))

(t/deftest reactive-entity
  (t/testing "Reactive entities can be queried"
    (let [conn (db/create)

          id 42
          u (random-uuid)
          name "UXBox"
          width 400
          height 500

          a (qs/rentity id conn)]
      (t/is (= @a
               {:db/id 42}))

      (d/transact! conn [[:db/add 42 :project/uuid u]])
      (t/is (= @a
               {:db/id 42
                :project/uuid u
                }))

      (d/transact! conn [[:db/add 42 :project/name name]])
      (t/is (= @a
               {:db/id 42
                :project/uuid u
                :project/name name}))

      (d/transact! conn [[:db/add 42 :project/width width]])
      (t/is (= @a
               {:db/id 42
                :project/uuid u
                :project/name name
                :project/width width}))

      (d/transact! conn [[:db/add 42 :project/name "Neo UXBox"]
                         [:db/add 42 :project/height height]])
      (t/is (= @a
               {:db/id 42
                :project/uuid u
                :project/name "Neo UXBox"
                :project/width width
                :project/height height})))))

(t/deftest reactive-pull-entity
  (t/testing "Reactive entities can be queried with a pull"
    (let [conn (db/create)

          id 42
          u (random-uuid)
          name "UXBox"
          width 400
          height 500

          pull '[:project/uuid
                 :project/name
                 :project/width
                 :project/height]

          a (qs/rentity id pull conn)]
      (t/is (= @a
               nil))

      (d/transact! conn [[:db/add 42 :project/uuid u]])
      (t/is (= @a
               {:project/uuid u
                }))

      (d/transact! conn [[:db/add 42 :project/name name]])
      (t/is (= @a
               {:project/uuid u
                :project/name name}))

      (d/transact! conn [[:db/add 42 :project/width width]])
      (t/is (= @a
               {:project/uuid u
                :project/name name
                :project/width width}))

      (d/transact! conn [[:db/add 42 :project/name "Neo UXBox"]
                         [:db/add 42 :project/height height]])
      (t/is (= @a
               {:project/uuid u
                :project/name "Neo UXBox"
                :project/width width
                :project/height height})))))
