(ns uxbox.test.shapes.data-test
  (:require
   [uxbox.log.core :as log]
   [uxbox.data.db :as db]
   [uxbox.shapes.queries :as q]
   [uxbox.projects.data :as pd]
   [uxbox.shapes.data :as sd]
   [uxbox.shapes.protocols :as proto]
   [cljs.test :as t]))


(defrecord Line [x1 y1 x2 y2])

(t/deftest shapes
  (t/testing "Shapes can be added to and removed from a page"
    (let [conn (db/create)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop

          page1 (random-uuid)
          title1 "A page"

          shape1 (random-uuid)]
      ;; Create project
      (log/record! conn
                   :uxbox/create-project
                    (pd/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (pd/create-page page1 pid title1 width height))

      ;; Add shape to page
      (let [rshape (Line. 0 0 1 1)]
        (log/record! conn
                     :uxbox/create-shape
                      (sd/create-shape shape1 page1 (Line. 0 0 1 1)))

        (t/is (= 1 (q/shape-count-by-page-id page1 @conn)))

        (let [{suuid :shape/uuid
               sdata :shape/data} (q/pull-shape-by-id shape1 @conn)]
          (t/is (= suuid shape1))
          (t/is (= sdata rshape))))

      (let [{suuid :shape/uuid
             sdata :shape/data} (q/pull-shape-by-id shape1 @conn)]
          (t/is (= sdata (Line. 0 0 1 1))))

      ;; Delete shape
      (log/record! conn :uxbox/delete-shape shape1)

      (t/is (= 0 (q/shape-count-by-page-id page1 @conn)))))

  (t/testing "Shapes can be updated in bulk"
    (let [conn (db/create)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop

          page1 (random-uuid)
          title1 "A page"

          shape1 (random-uuid)]
      ;; Create project
      (log/record! conn
                   :uxbox/create-project
                    (pd/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (pd/create-page page1 pid title1 width height))

      ;; Add shape to page
      (let [rshape (Line. 0 0 1 1)]
        (log/record! conn
                     :uxbox/create-shape
                      (sd/create-shape shape1 page1 (Line. 0 0 1 1)))

        (let [{sdata :shape/data
               :as shape} (q/pull-shape-by-id shape1 @conn)
              ndata (assoc sdata ::foo ::foo)
              _ (log/record! conn :uxbox/update-shapes [[shape1 ndata]])
              nshape (q/pull-shape-by-id shape1 @conn)]
          (t/is (= ndata (:shape/data nshape)))))))

  (t/testing "Shape attributes can be changed"
    (let [conn (db/create)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop

          page1 (random-uuid)
          title1 "A page"

          shape1 (random-uuid)]
      ;; Create project
      (log/record! conn
                   :uxbox/create-project
                    (pd/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (pd/create-page page1 pid title1 width height))
      ;; Add shape to page
      (log/record! conn
                   :uxbox/create-shape
                   (sd/create-shape shape1 page1 (Line. 0 0 1 1)))

      ;; Change shape attribute
      (log/record! conn
                   :uxbox/change-shape
                   [shape1 :x1 42])

      (let [{sdata :shape/data} (q/pull-shape-by-id shape1 @conn)]
          (t/is (= sdata (Line. 42 0 1 1))))))

  (t/testing "Visible and locked flag can be toggled"
    (let [conn (db/create)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop

          page1 (random-uuid)
          title1 "A page"

          shape1 (random-uuid)]
      ;; Create project
      (log/record! conn
                   :uxbox/create-project
                    (pd/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                   (pd/create-page page1 pid title1 width height))
      ;; Add shape to page
      (log/record! conn
                   :uxbox/create-shape
                   (sd/create-shape shape1 page1 (Line. 0 0 1 1)))

      ;; Toggle visibility
      (t/is (:shape/visible? (q/pull-shape-by-id shape1 @conn)))

      (log/record! conn :uxbox/toggle-shape-visibility shape1)

      (t/is (not (:shape/visible? (q/pull-shape-by-id shape1 @conn))))

      (log/record! conn :uxbox/toggle-shape-visibility shape1)

      (t/is (:shape/visible? (q/pull-shape-by-id shape1 @conn)))

      ;; Toggle locking
      (t/is (not (:shape/locked? (q/pull-shape-by-id shape1 @conn))))

      (log/record! conn :uxbox/toggle-shape-lock shape1)

      (t/is (:shape/locked? (q/pull-shape-by-id shape1 @conn)))

      (log/record! conn :uxbox/toggle-shape-lock shape1)

      (t/is (not (:shape/locked? (q/pull-shape-by-id shape1 @conn)))))))
