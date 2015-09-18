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
          vals (volatile! [])
          s (qs/rquery '[:find [?n ...]
                         :where
                         [?e :name ?n]
                         [?e :cool? true]]
                        conn)]
      (s/on-value s #(vswap! vals conj %))

      (d/transact! conn [{:name "John"
                          :cool? true}])

      (d/transact! conn [{:name "Mariano"
                          :cool? false}])

      (d/transact! conn [{:db/id 42
                          :name "Grace"
                          :cool? true}
                         {:name "Ada"
                          :cool? true}])

      (d/transact! conn [[:db/add 42 :cool? false]])

      (t/is (= @vals
              [[]

               ["John"]

               ["John" "Grace" "Ada"]

               ["John" "Ada"]])))))

(t/deftest reactive-pull-query
  (t/testing "Reactive pull queries can be constructed"
    (let [conn (db/create)
          vals (volatile! [])
          query '[:find [?e ...]
                  :where [?e :name ?n]
                         [?e :cool? true]]
          pull '[:name]
          s (qs/rpull query pull conn)]
      (s/on-value s #(vswap! vals conj %))

      (d/transact! conn [{:name "John"
                          :cool? true}])

      (d/transact! conn [{:name "Mariano"
                          :cool? false}])

      (d/transact! conn [{:name "Grace"
                          :cool? true}
                         {:db/id 42
                          :name "Ada"
                          :cool? true}])

      (d/transact! conn [[:db/add 42 :name "Ada Lovelace"]])

      (t/is (= @vals
              [[]

               [{:name "John"}]

               [{:name "John"}
                {:name "Grace"}
                {:name "Ada"}]

               [{:name "John"}
                {:name "Grace"}
                {:name "Ada Lovelace"}]])))))

(t/deftest reactive-entity
  (t/testing "Reactive entities can be queried"
    (let [conn (db/create)
          vals (volatile! [])

          id 42
          u (random-uuid)
          name "UXBox"
          width 400
          height 500

          s (qs/rentity id conn)]

      (s/on-value s #(vswap! vals conj %))

      (d/transact! conn [[:db/add 42 :project/uuid u]])

      ;; note: stream should be deduped
      (d/transact! conn [[:db/add 42 :project/name name]])
      (d/transact! conn [[:db/add 42 :project/name name]])

      (d/transact! conn [[:db/add 42 :project/width width]])

      (d/transact! conn [[:db/add 42 :project/name "Neo UXBox"]
                         [:db/add 42 :project/height height]])

      (t/is (= @vals
               [{:db/id 42}

                {:db/id 42
                 :project/uuid u}

                {:db/id 42
                 :project/uuid u
                 :project/name name}

                {:db/id 42
                 :project/uuid u
                 :project/name name
                 :project/width width}

                {:db/id 42
                 :project/uuid u
                 :project/name "Neo UXBox"
                 :project/width width
                 :project/height height}])))))

(t/deftest reactive-pull-entity
  (t/testing "Reactive entities can be queried with a pull"
    (let [conn (db/create)
          vals (volatile! [])

          id 42
          u (random-uuid)
          name "UXBox"
          width 400
          height 500

          pull '[:project/uuid
                 :project/name
                 :project/width
                 :project/height]

          s (qs/rentity id pull conn)]

      (s/on-value s #(vswap! vals conj %))

      (d/transact! conn [[:db/add 42 :project/uuid u]])

      ;; note: stream should be deduped
      (d/transact! conn [[:db/add 42 :project/name name]])
      (d/transact! conn [[:db/add 42 :project/name name]])

      (d/transact! conn [[:db/add 42 :project/width width]])

      (d/transact! conn [[:db/add 42 :project/name "Neo UXBox"]
                         [:db/add 42 :project/height height]])

      (t/is (= @vals
               [nil

                {:project/uuid u}

                {:project/uuid u
                 :project/name name}

                {:project/uuid u
                 :project/name name
                 :project/width width}

                {:project/uuid u
                 :project/name "Neo UXBox"
                 :project/width width
                 :project/height height}])))))
