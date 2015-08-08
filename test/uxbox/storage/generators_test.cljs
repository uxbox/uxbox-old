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

(t/deftest groups-data-generator
  (t/testing "That event manage correctly create group"
    (reset! v/groups-view {})
    (g/groups-data {:type :create-group :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @v/groups-view "123"))
    (t/is (= (get @v/groups-view "123") {:uuid "123" :some-extra-data :test})))

  (t/testing "That event manage correctly toggle group lock"
    (reset! v/groups-view {"123" {:locked true}})
    (g/groups-data {:type :toggle-group-lock :data {:group-uuid "123"}})
    (t/is (not (get-in @v/groups-view ["123" :locked])))
    (g/groups-data {:type :toggle-group-lock :data {:group-uuid "123"}})
    (t/is (get-in @v/groups-view ["123" :locked])))

  (t/testing "That event manage correctly toggle group visibility"
    (reset! v/groups-view {"123" {:visible true}})
    (g/groups-data {:type :toggle-group-visibility :data {:group-uuid "123"}})
    (t/is (not (get-in @v/groups-view ["123" :visible])))
    (g/groups-data {:type :toggle-group-visibility :data {:group-uuid "123"}})
    (t/is (get-in @v/groups-view ["123" :visible])))

  (t/testing "That event manage correctly delete group"
    (reset! v/groups-view {"123" {:data :data} "456" {:data :data}})
    (g/groups-data {:type :delete-group :data {:group-uuid "123"}})
    (t/is (contains? @v/groups-view "456"))
    (t/is (not (contains? @v/groups-view "123"))))

  (t/testing "That event manage correctly delete page"
    (reset! v/groups-view {"123" {:page-uuid "deleted-project"} "456" {:page-uuid "not-deleted-project"}})
    (g/groups-data {:type :delete-page :data {:page-uuid "deleted-project"}})
    (t/is (contains? @v/groups-view "456"))
    (t/is (not (contains? @v/groups-view "123"))))

  (t/testing "That event manage correctly delete project"
    (reset! v/groups-view {"123" {:project-uuid "deleted-project"} "456" {:project-uuid "not-deleted-project"}})
    (g/groups-data {:type :delete-project :data {:project-uuid "deleted-project"}})
    (t/is (contains? @v/groups-view "456"))
    (t/is (not (contains? @v/groups-view "123")))))

(t/deftest shapes-data-generator
  (t/testing "That event manage correctly create shape"
    (reset! v/shapes-view {})
    (g/shapes-data {:type :create-shape :data {:uuid "123" :some-extra-data :test}})
    (t/is (contains? @v/shapes-view "123"))
    (t/is (= (get @v/shapes-view "123") {:uuid "123" :some-extra-data :test})))

  (t/testing "That event manage correctly delete shape"
    (reset! v/shapes-view {"123" {:data :data} "456" {:data :data}})
    (g/shapes-data {:type :delete-shape :data {:shape-uuid "123"}})
    (t/is (contains? @v/shapes-view "456"))
    (t/is (not (contains? @v/shapes-view "123"))))

  (t/testing "That event manage correctly delete shape"
    (reset! v/shapes-view {"123" {:group-uuid "deleted-project"} "456" {:group-uuid "not-deleted-project"}})
    (g/shapes-data {:type :delete-group :data {:group-uuid "deleted-project"}})
    (t/is (contains? @v/shapes-view "456"))
    (t/is (not (contains? @v/shapes-view "123"))))

  (t/testing "That event manage correctly delete page"
    (reset! v/shapes-view {"123" {:page-uuid "deleted-project"} "456" {:page-uuid "not-deleted-project"}})
    (g/shapes-data {:type :delete-page :data {:page-uuid "deleted-project"}})
    (t/is (contains? @v/shapes-view "456"))
    (t/is (not (contains? @v/shapes-view "123"))))

  (t/testing "That event manage correctly delete project"
    (reset! v/shapes-view {"123" {:project-uuid "deleted-project"} "456" {:project-uuid "not-deleted-project"}})
    (g/shapes-data {:type :delete-project :data {:project-uuid "deleted-project"}})
    (t/is (contains? @v/shapes-view "456"))
    (t/is (not (contains? @v/shapes-view "123")))))
