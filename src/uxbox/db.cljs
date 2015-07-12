(ns uxbox.db
  (:require [reagent.core :as reagent :refer [atom]]))

(def initial-state {:location [:login]
                    :lightbox nil
                    :user {:fullname "Michael Buchannon"
                           :avatar "/images/avatar.jpg"}
                    :activity []
                    :project-sort-order :name
                    :project-orderings {
                      :name "name"
                      :last-update "date updated"
                      :created "date created"
                    }
                    :new-project-name ""
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
