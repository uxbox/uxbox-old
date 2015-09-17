(ns uxbox.test.projects.data-test
  (:require
   [uxbox.log.core :as log]
   [uxbox.data.db :as db]
   [uxbox.projects.queries :as q]
   [uxbox.projects.data :as p]
   [cljs.test :as t]))

(t/deftest project
  (t/testing "A project can be created and deleted"
    (let [conn (db/create)
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
    (let [conn (db/create)
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
    (let [conn (db/create)
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
