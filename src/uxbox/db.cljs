(ns uxbox.db
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce a-project-id (random-uuid))
(defonce another-project-id (random-uuid))

(def initial-state {:location [:login]
                    :login-form :login
                    :lightbox nil
                    :default-open-setting-boxes #{:tools :layers}
                    :open-setting-boxes #{:tools :layers}
                    :open-user-menu false
                    :user {:fullname "Michael Buchannon"
                           :avatar "/images/avatar.jpg"}
                    :activity []
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
                    :projects-list {}
                    :new-project-defaults {
                      :name ""
                      :width 1920
                      :height 1080
                      :layout :desktop
                    }
                    :visible-project-bar false
                    :new-project-name ""
                    :editing-pages {}
                    :new-page-name ""
                    :adding-new-page false
                    :workspace-defaults {:selected-tool nil
                                         :selected-element 0
                                         :grid false}
                    :workspace {:selected-tool nil
                                :selected-groups #{}
                                :selected-element 0
                                :grid false}

                    ;; Rectangles: http://www.w3.org/TR/SVG/shapes.html#RectElement
                    ;; Lines: http://www.w3.org/TR/SVG/shapes.html#LineElement
                    ;; Style properties http://www.w3.org/TR/SVG/propidx.html
                    :current-catalog :material-design-actions
                    :scroll {:top 0 :left 0}

                    :project nil
                    :page nil})

(defonce app-state (atom initial-state))
