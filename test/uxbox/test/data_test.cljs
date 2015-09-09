(ns uxbox.test.data-test
  (:require
   [datascript :as d]
   [uxbox.data.log :as log]
   [uxbox.data.schema :as s]
   [uxbox.data.queries :as q]
   [uxbox.data.projects :as p]
   [cljs.test :as t]))

(t/deftest create-project
  (t/testing "A project is stored after created"
    (let [conn (d/create-conn s/schema)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop]
      (t/is (nil? (q/project-by-id pid @conn)))

      (log/persist! :uxbox/create-project
                    (p/create-project pid name width height layout)
                    conn)

      (t/is (q/project-by-id pid @conn))
      (let [p (q/pull-project-by-id pid @conn)]
        (t/is (= (:project/name p) name))
        (t/is (= (:project/width p) width))
        (t/is (= (:project/height p) height))
        (t/is (= (:project/layout p) layout))))))

(t/deftest delete-project
  (t/testing "A project can be deleted"
    (let [conn (d/create-conn s/schema)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop]
      (log/persist! :uxbox/create-project
                    (p/create-project pid name width height layout)
                    conn)
      (t/is (q/project-by-id pid @conn))

      (log/persist! :uxbox/delete-project pid conn)

      (t/is (nil? (q/project-by-id pid @conn))))))
