(ns uxbox.db
  (:require [reagent.core :as reagent :refer [atom]]))

(def app-state (atom))

(def initial-state {:location [:home]
                    :user {:fullname "Michael Buchannon"
                             :avatar "/images/avatar.jpg"}
                      :activity []
                      :project-sort-order :name
                      :project-orderings {
                        :name "name"
                        :last-update "date updated"
                        :created "date created"
                      }
                      :projects [
                        {:name "Wireframes Taiga Tribe"
                         :uuid (random-uuid)
                         :last-update (js/Date. 2020 10 1)
                         :created (js/Date. 2020 9 1)
                         :page-count 3
                         :comment-count 6}
                        {:name "A WYSH Roadmap"
                         :uuid (random-uuid)
                         :last-update (js/Date. 2014 10 1)
                         :created (js/Date. 2014 9 1)
                         :page-count 2
                         :comment-count 4}
                      ]})
