(ns uxbox.storage.generators-test
  (:require [cljs.test :as t]
            [uxbox.storage.generators :as g]
            [uxbox.storage.views :as v]))


(t/deftest projects-data-generator
  (t/testing "That event manage correctly create project"
    (reset! v/projects-view {})
    (g/projects-data {:type :create-project :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @v/projects-view "123"))
    (t/is (= (get @v/projects-view "123") {:uuid "123" :some-extra-data :test :pages 0 :comments 0})))

  (t/testing "That event manage correctly delete project"
    (reset! v/projects-view {"123" {:data :data} "456" {:data :data}})
    (g/projects-data {:type :delete-project :data {:project-uuid "123"}})
    (t/is (contains? @v/projects-view "456"))
    (t/is (not (contains? @v/projects-view "123"))))

  (t/testing "That event manage correctly create page"
    (reset! v/projects-view {"123" {:pages 0}})
    (g/projects-data {:type :create-page :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :pages]) 1))
    (g/projects-data {:type :create-page :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :pages]) 2)))

  (t/testing "That event manage correctly delete page"
    (reset! v/projects-view {"123" {:pages 10}})
    (g/projects-data {:type :delete-page :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :pages]) 9))
    (g/projects-data {:type :delete-page :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :pages]) 8)))

  (t/testing "That event manage correctly create comment"
    (reset! v/projects-view {"123" {:comments 0}})
    (g/projects-data {:type :create-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :comments]) 1))
    (g/projects-data {:type :create-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :comments]) 2)))

  (t/testing "That event manage correctly delete comment"
    (reset! v/projects-view {"123" {:comments 10}})
    (g/projects-data {:type :delete-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :comments]) 9))
    (g/projects-data {:type :delete-comment :data {:project-uuid "123"}})
    (t/is (= (get-in @v/projects-view ["123" :comments]) 8))))

(t/deftest pages-data-generator
  (t/testing "That event manage correctly create page"
    (reset! v/pages-view {})
    (g/pages-data {:type :create-page :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @v/pages-view "123"))
    (t/is (= (get @v/pages-view "123") {:uuid "123" :some-extra-data :test})))

  (t/testing "That event manage correctly delete page"
    (reset! v/pages-view {"123" {:data :data} "456" {:data :data}})
    (g/pages-data {:type :delete-page :data {:page-uuid "123"}})
    (t/is (contains? @v/pages-view "456"))
    (t/is (not (contains? @v/pages-view "123"))))

  (t/testing "That event manage correctly delete project"
    (reset! v/pages-view {"123" {:project-uuid "deleted-project"} "456" {:project-uuid "not-deleted-project"}})
    (g/pages-data {:type :delete-project :data {:project-uuid "deleted-project"}})
    (t/is (contains? @v/pages-view "456"))
    (t/is (not (contains? @v/pages-view "123")))))
