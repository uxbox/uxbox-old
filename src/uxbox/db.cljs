(ns uxbox.db
  (:require [reagent.core :as reagent :refer [atom]]))

(def a-project-id (random-uuid))
(def another-project-id (random-uuid))

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
                    :new-project-name ""
                    :workspace {:selected-tool :rect
                                :selected-element 0}
                    :projects [
                      {:name "Design of UXBox"
                       :uuid (random-uuid)
                       :last-update (js/Date. 2014 10 1)
                       :created (js/Date. 2014 9 1)
                       :page-count 3
                       :comment-count 6}
                      {:name "Wireframes Taiga Tribe"
                       :uuid (random-uuid)
                       :last-update (js/Date. 2005 10 1)
                       :created (js/Date. 2005 9 1)
                       :page-count 3
                       :comment-count 6}
                      {:name "A WYSH Roadmap"
                       :uuid (random-uuid)
                       :last-update (js/Date. 2010 10 1)
                       :created (js/Date. 2010 9 1)
                       :page-count 2
                       :comment-count 4}]})

(defonce app-state (atom initial-state))
