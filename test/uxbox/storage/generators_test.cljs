(ns uxbox.storage.generators-test
  (:require [cljs.test :as t]
            [uxbox.shapes.rectangle :refer [new-rectangle]]
            [uxbox.shapes.circle :refer [new-circle]]
            [uxbox.shapes.line :refer [new-line]]
            [uxbox.shapes.path :refer [new-path-shape]]
            [uxbox.storage.generators :as g]
            [uxbox.storage.atoms :as a]))


(t/deftest projects-data-generator
  (t/testing "That event manage correctly create project"
    (reset! a/projects-view {})
    (g/projects-data {:type :create-project :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @a/projects-view "123"))
    (t/is (= (get @a/projects-view "123") {:uuid "123" :some-extra-data :test :pages 0 :comments 0})))

  (t/testing "That event manage correctly delete project"
    (reset! a/projects-view {"123" {:data :data} "456" {:data :data}})
    (g/projects-data {:type :delete-project :data {:project-uuid "123"}})
    (t/is (contains? @a/projects-view "456"))
    (t/is (not (contains? @a/projects-view "123"))))

  (t/testing "That event manage correctly create page"
    (reset! a/projects-view {"123" {:pages 0}})
    (g/projects-data {:type :create-page :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :pages]) 1))
    (g/projects-data {:type :create-page :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :pages]) 2)))

  (t/testing "That event manage correctly delete page"
    (reset! a/projects-view {"123" {:pages 10}})
    (g/projects-data {:type :delete-page :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :pages]) 9))
    (g/projects-data {:type :delete-page :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :pages]) 8)))

  (t/testing "That event manage correctly create comment"
    (reset! a/projects-view {"123" {:comments 0}})
    (g/projects-data {:type :create-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :comments]) 1))
    (g/projects-data {:type :create-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :comments]) 2)))

  (t/testing "That event manage correctly delete comment"
    (reset! a/projects-view {"123" {:comments 10}})
    (g/projects-data {:type :delete-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :comments]) 9))
    (g/projects-data {:type :delete-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @a/projects-view ["123" :comments]) 8))))

(t/deftest pages-data-generator
  (t/testing "That event manage correctly create page"
    (reset! a/pages-view {})
    (g/pages-data {:type :create-page :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @a/pages-view "123"))
    (t/is (= (get @a/pages-view "123") {:uuid "123" :some-extra-data :test})))

  (t/testing "That event manage correctly delete page"
    (reset! a/pages-view {"123" {:data :data} "456" {:data :data}})
    (g/pages-data {:type :delete-page :data {:page-uuid "123"}})
    (t/is (contains? @a/pages-view "456"))
    (t/is (not (contains? @a/pages-view "123"))))

  (t/testing "That event manage correctly change page title"
    (reset! a/pages-view {"123" {:title "test"} "456" {:title "test2"}})
    (g/pages-data {:type :change-page-title :data {:page-uuid "123" :new-title "test3"}})
    (t/is (get-in @a/pages-view ["456" :title]) "test2")
    (t/is (get-in @a/pages-view ["123" :title]) "test3"))

  (t/testing "That event manage correctly delete project"
    (reset! a/pages-view {"123" {:project-uuid "deleted-project"} "456" {:project-uuid "not-deleted-project"}})
    (g/pages-data {:type :delete-project :data {:project-uuid "deleted-project"}})
    (t/is (contains? @a/pages-view "456"))
    (t/is (not (contains? @a/pages-view "123")))))

(t/deftest shapes-data-generator
  (t/testing "That event manage correctly create shape"
    (reset! a/shapes-view {})
    (g/shapes-data {:type :create-shape :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @a/shapes-view "123"))
    (t/is (= (get @a/shapes-view "123") {:uuid "123" :some-extra-data :test})))

  (t/testing "That event manage correctly delete shape"
    (reset! a/shapes-view {"123" {:data :data} "456" {:data :data}})
    (g/shapes-data {:type :delete-shape :data {:shape-uuid "123"}})
    (t/is (contains? @a/shapes-view "456"))
    (t/is (not (contains? @a/shapes-view "123"))))

  (t/testing "That event manage correctly delete page"
    (reset! a/shapes-view {"123" {:page-uuid "deleted-project"} "456" {:page-uuid "not-deleted-project"}})
    (g/shapes-data {:type :delete-page :data {:page-uuid "deleted-project"}})
    (t/is (contains? @a/shapes-view "456"))
    (t/is (not (contains? @a/shapes-view "123"))))

  (t/testing "That event manage correctly delete project"
    (reset! a/shapes-view {"123" {:project-uuid "deleted-project"} "456" {:project-uuid "not-deleted-project"}})
    (g/shapes-data {:type :delete-project :data {:project-uuid "deleted-project"}})
    (t/is (contains? @a/shapes-view "456"))
    (t/is (not (contains? @a/shapes-view "123"))))

  (t/testing "That event manage correctly move shape on Rectangle"
    (reset! a/shapes-view {"123" (new-rectangle 10 10 30 30) "456" (new-rectangle 20 20 40 40)})
    (g/shapes-data {:type :move-shape :data {:shape-uuid "123" :delta-x 30 :delta-y 40}})
    (t/is (get-in @a/shapes-view ["456" :x]) 20)
    (t/is (get-in @a/shapes-view ["456" :y]) 20)
    (t/is (get-in @a/shapes-view ["456" :width]) 40)
    (t/is (get-in @a/shapes-view ["456" :height]) 40)
    (t/is (get-in @a/shapes-view ["123" :x]) 40)
    (t/is (get-in @a/shapes-view ["123" :y]) 50)
    (t/is (get-in @a/shapes-view ["123" :width]) 30)
    (t/is (get-in @a/shapes-view ["123" :height]) 30))

  (t/testing "That event manage correctly move shape on Circle"
    (reset! a/shapes-view {"123" (new-circle 10 10 30) "456" (new-circle 20 20 40)})
    (g/shapes-data {:type :move-shape :data {:shape-uuid "123" :delta-x 30 :delta-y 40}})
    (t/is (get-in @a/shapes-view ["456" :cx]) 20)
    (t/is (get-in @a/shapes-view ["456" :cy]) 20)
    (t/is (get-in @a/shapes-view ["456" :r]) 40)
    (t/is (get-in @a/shapes-view ["123" :cx]) 40)
    (t/is (get-in @a/shapes-view ["123" :cy]) 50)
    (t/is (get-in @a/shapes-view ["123" :r]) 30))

  (t/testing "That event manage correctly move shape on Line"
    (reset! a/shapes-view {"123" (new-line 10 10 30 30) "456" (new-line 20 20 40 40)})
    (g/shapes-data {:type :move-shape :data {:shape-uuid "123" :delta-x 30 :delta-y 40}})
    (t/is (get-in @a/shapes-view ["456" :x1]) 20)
    (t/is (get-in @a/shapes-view ["456" :y1]) 20)
    (t/is (get-in @a/shapes-view ["456" :x2]) 40)
    (t/is (get-in @a/shapes-view ["456" :y2]) 40)
    (t/is (get-in @a/shapes-view ["123" :x1]) 40)
    (t/is (get-in @a/shapes-view ["123" :y1]) 50)
    (t/is (get-in @a/shapes-view ["123" :x2]) 60)
    (t/is (get-in @a/shapes-view ["123" :y2]) 70))

  (t/testing "That event manage correctly move shape on Path"
    (reset! a/shapes-view {"123" (new-path-shape 10 10 30 30 "test1" 15 15) "456" (new-path-shape 20 20 40 40 "test2" 25 25)})
    (g/shapes-data {:type :move-shape :data {:shape-uuid "123" :delta-x 30 :delta-y 40}})
    (t/is (get-in @a/shapes-view ["456" :x]) 20)
    (t/is (get-in @a/shapes-view ["456" :y]) 20)
    (t/is (get-in @a/shapes-view ["456" :width]) 40)
    (t/is (get-in @a/shapes-view ["456" :height]) 40)
    (t/is (get-in @a/shapes-view ["456" :path]) "test2")
    (t/is (get-in @a/shapes-view ["456" :icowidth]) 25)
    (t/is (get-in @a/shapes-view ["456" :icoheight]) 25)
    (t/is (get-in @a/shapes-view ["123" :x]) 40)
    (t/is (get-in @a/shapes-view ["123" :y]) 50)
    (t/is (get-in @a/shapes-view ["123" :width]) 30)
    (t/is (get-in @a/shapes-view ["123" :height]) 30)
    (t/is (get-in @a/shapes-view ["123" :path]) "test1")
    (t/is (get-in @a/shapes-view ["123" :icowidth]) 15)
    (t/is (get-in @a/shapes-view ["123" :icoheight]) 15))

  (t/testing "That event manage correctly change shape attr"
    (reset! a/shapes-view {"123" {:name "test"} "456" {:name "test2"}})
    (g/shapes-data {:type :change-shape-attr :data {:shape-uuid "123" :attr :name :value "test3"}})
    (t/is (get-in @a/shapes-view ["456" :name]) "test2")
    (t/is (get-in @a/shapes-view ["123" :name]) "test3")))
