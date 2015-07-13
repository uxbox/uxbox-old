(ns uxbox.db
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce a-project-id (random-uuid))
(defonce another-project-id (random-uuid))

(def sample-projects [{:name "Design of UXBox"
                       :uuid a-project-id
                       :last-update (js/Date. 2014 10 1)
                       :created (js/Date. 2014 9 1)
                       :page-count 3
                       :comment-count 6}
                      {:name "Wireframes Taiga Tribe"
                       :uuid another-project-id
                       :last-update (js/Date. 2005 10 1)
                       :created (js/Date. 2005 9 1)
                       :page-count 3
                       :comment-count 6}
                      {:name "A WYSH Roadmap"
                       :uuid (random-uuid)
                       :last-update (js/Date. 2010 10 1)
                       :created (js/Date. 2010 9 1)
                       :page-count 2
                       :comment-count 3}])

(def initial-state {:location [:login]
                    :lightbox nil
                    :open-setting-boxes #{:tools :layers}
                    :user {:fullname "Michael Buchannon"
                           :avatar "/images/avatar.jpg"}
                    :activity [
                      {:author {:name "Other user" :avatar "../../images/avatar.jpg" }
                       :project {:uuid  a-project-id :name "Design of UXBox"}
                       :datetime (js/Date. 2015 6 13 17 15)
                       :event {:type "create new page" :name "Login"}}
                      {:author {:name "Michael Buchannon" :avatar "../../images/avatar.jpg"}
                       :project {:uuid  another-project-id :name "Wireframes Taiga Tribe"}
                       :datetime (js/Date. 2015 6 12 17 00)
                       :event {:type "create new page" :name "Login"}}
                      {:author {:name "Michael Buchannon" :avatar "../../images/avatar.jpg"}
                       :project {:uuid  another-project-id :name "Wireframes Taiga Tribe"}
                       :datetime (js/Date. 2015 6 11 17 00)
                       :event {:type "create new page" :name "Login"}}
                      {:author {:name "Michael Buchannon" :avatar "../../images/avatar.jpg"}
                       :project {:uuid  another-project-id :name "Wireframes Taiga Tribe"}
                       :datetime (js/Date. 2015 6 11 17 00)
                       :event {:type "create new page" :name "Login"}}
                      {:author {:name "Michael Buchannon" :avatar "../../images/avatar.jpg"}
                       :project {:uuid  another-project-id :name "Wireframes Taiga Tribe"}
                       :datetime (js/Date. 2015 6 10 17 00)
                       :event {:type "create new page" :name "Login"}}]
                    :project-sort-order :name
                    :project-orderings {
                      :name "name"
                      :last-update "date updated"
                      :created "date created"
                    }
                    :project-layouts {
                      :mobile {:name "Mobile" :width 320 :height 480}
                      :tablet {:name "Tablet" :width 1024 :height 768}
                      :notebook {:name "Notebook" :width 1366 :height 768}
                      :desktop {:name "Desktop" :width 1920 :height 1080}
                    }
                    :new-project-defaults {
                      :name ""
                    }
                    :workspace {:selected-tool :rect
                                :selected-element 0}
                    :projects (into {} (for [p sample-projects
                                             :let [uuid (:uuid p)]]
                                         [uuid p]))

                    ;; Rectangles: http://www.w3.org/TR/SVG/shapes.html#RectElement
                    ;; Lines: http://www.w3.org/TR/SVG/shapes.html#LineElement
                    ;; Style properties http://www.w3.org/TR/SVG/propidx.html

                    :page

                    {:title "My awesome page"
                     :author "Bobby Tables"

                     :width 640
                     :height 1080

                     :shapes {"id1" {:shape :rectangle
                                     :x 0 :y 0 :width 200 :height 200 :fill "#cacaca" :stroke "black"} ;; Rectangle
                              "id2" {:shape :rectangle
                                     :x 20 :y 20 :width 160 :height 160 :rx 5 :ry 5 :fill "white" :stroke "#cacaca"} ;; Rounded rectangle
                              "id3" {:shape :line
                                     :x1 20 :y1 20 :x2 180 :y2 180 :color "blue" :width 4}
                              "id4" {:shape :line
                                     :x1 180 :y1 20 :x2 20 :y2 180 :color "blue" :width 4}}

                     :groups {"gid1" {:name "Box 1" :order 1 :visible true  :locked false :icon :square :shapes ["id1"]}
                              "gid2" {:name "Box 2" :order 2 :visible true  :locked false :icon :circle :shapes ["id2"]}
                              "gid3" {:name "Cross" :order 3 :visible true  :locked false :icon :line   :shapes ["id3" "id4"]}}}})

(defonce app-state (atom initial-state))
