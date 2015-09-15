(ns uxbox.test.data-test
  (:require
   [datascript :as d]
   [uxbox.log.core :as log]
   [uxbox.data.schema :as s]
   [uxbox.data.queries :as q]
   [uxbox.projects.data :as p]
   [uxbox.shapes.protocols :as proto]
   [cljs.test :as t]))


(t/deftest project
  (t/testing "A project can be created and deleted"
    (let [conn (d/create-conn s/schema)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop]
      ;; Create
      (log/record! conn
                   :uxbox/create-project
                   (p/create-project pid name width height layout))

      (t/is (q/project-by-id pid @conn))
      (let [{pname :project/name
             pwidth :project/width
             pheight :project/height
             playout :project/layout} (q/pull-project-by-id pid @conn)]
        (t/is (= pname name))
        (t/is (= pwidth width))
        (t/is (= pheight height))
        (t/is (= playout layout)))

      ;; Delete
      (log/record! conn :uxbox/delete-project pid)

      (t/is (nil? (q/project-by-id pid @conn))))))

(t/deftest pages
  (t/testing "Pages can be added to and removed from a project"
    (let [conn (d/create-conn s/schema)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop

          page1 (random-uuid)
          page2 (random-uuid)
          title1 "A page"
          title2 "Another page"]
      ;; Create project
      (log/record! conn
                   :uxbox/create-project
                    (p/create-project pid name width height layout))

      (t/is (= 0 (q/page-count-by-project-id pid @conn)))

      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (p/create-page page1 pid title1 width height))

      (t/is (= 1 (q/page-count-by-project-id pid @conn)))
      (let [{ptitle :page/title
             pwidth :page/width
             pheight :page/height} (q/pull-page-by-id page1 @conn)]
        (t/is (= ptitle title1))
        (t/is (= pwidth width))
        (t/is (= pheight height)))

      ;; Add another page
      (log/record! conn
                   :uxbox/create-page
                   (p/create-page page2 pid title2 width height))

      (t/is (= 2 (q/page-count-by-project-id pid @conn)))
      (let [{ptitle :page/title
             pwidth :page/width
             pheight :page/height} (q/pull-page-by-id page2 @conn)]
        (t/is (= ptitle title2))
        (t/is (= pwidth width))
        (t/is (= pheight height)))

      ;; Delete pages
      (log/record! conn :uxbox/delete-page page1)

      (t/is (= 1 (q/page-count-by-project-id pid @conn)))
      (t/is (nil? (q/page-by-id page1 @conn)))

      (log/record! conn :uxbox/delete-page page2)

      (t/is (= 0 (q/page-count-by-project-id pid @conn)))))

  (t/testing "Page titles can be edited"
    (let [conn (d/create-conn s/schema)
          pid (random-uuid)
          name "A project"
          width 1920
          height 1080
          layout :desktop

          page1 (random-uuid)
          title1 "A page"]
      ;; Create project
      (log/record! conn
                   :uxbox/create-project
                    (p/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (p/create-page page1 pid title1 width height))

      (let [{ptitle :page/title} (q/pull-page-by-id page1 @conn)]
        (t/is (= ptitle title1)))

      ;; Change title
      (let [ntitle "A new page title"]
        (log/record! conn
                     :uxbox/change-page-title
                      [page1 ntitle])
        (let [{ptitle :page/title} (q/pull-page-by-id page1 @conn)]
          (t/is (= ptitle ntitle)))))))

(defrecord Line [x1 y1 x2 y2]
  proto/Shape
  (move-delta [_ dx dy]
    (Line. (+ x1 dx)
           (+ y1 dy)
           (+ x2 dx)
           (+ y2 dy))))

(t/deftest shapes
  (t/testing "Shapes can be added to and removed from a page"
    (let [conn (d/create-conn s/schema)
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
                    (p/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (p/create-page page1 pid title1 width height))

      ;; Add shape to page
      (let [rshape (Line. 0 0 1 1)]
        (log/record! conn
                     :uxbox/create-shape
                      (p/create-shape shape1 page1 (Line. 0 0 1 1)))

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
    (let [conn (d/create-conn s/schema)
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
                    (p/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (p/create-page page1 pid title1 width height))

      ;; Add shape to page
      (let [rshape (Line. 0 0 1 1)]
        (log/record! conn
                     :uxbox/create-shape
                      (p/create-shape shape1 page1 (Line. 0 0 1 1)))

        (let [{sdata :shape/data
               :as shape} (q/pull-shape-by-id shape1 @conn)
              ndata (assoc sdata ::foo ::foo)
              _ (log/record! conn :uxbox/update-shapes [[shape1 ndata]])
              nshape (q/pull-shape-by-id shape1 @conn)]
          (t/is (= ndata (:shape/data nshape)))))))

  (t/testing "Shape attributes can be changed"
    (let [conn (d/create-conn s/schema)
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
                    (p/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                    (p/create-page page1 pid title1 width height))
      ;; Add shape to page
      (log/record! conn
                   :uxbox/create-shape
                   (p/create-shape shape1 page1 (Line. 0 0 1 1)))

      ;; Change shape attribute
      (log/record! conn
                   :uxbox/change-shape
                   [shape1 :x1 42])

      (let [{sdata :shape/data} (q/pull-shape-by-id shape1 @conn)]
          (t/is (= sdata (Line. 42 0 1 1))))))

  (t/testing "Visible and locked flag can be toggled"
    (let [conn (d/create-conn s/schema)
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
                    (p/create-project pid name width height layout))
      ;; Add page
      (log/record! conn
                   :uxbox/create-page
                   (p/create-page page1 pid title1 width height))
      ;; Add shape to page
      (log/record! conn
                   :uxbox/create-shape
                   (p/create-shape shape1 page1 (Line. 0 0 1 1)))

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
