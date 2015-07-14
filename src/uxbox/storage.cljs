(ns uxbox.storage
  (:require [alandipert.storage-atom :refer [local-storage]]))

(def users [{:username "user-1"
             :password "user-1"}
            {:username "user-2"
             :password "user-2"}])

(def projects {"c078f148-2686-4b07-a165-c455a0ab19a7" {:name "Design UX Box"
                                                       :uuid "c078f148-2686-4b07-a165-c455a0ab19a7"
                                                       :last-update (js/Date. 2014 10 1)
                                                       :created (js/Date. 2014 9 1)
                                                       :owner "user-1"
                                                       :comments {
                                                         "3e14aaed-e3ab-45f4-9fc5-14dc023a545e" {:text "Comment1"}
                                                         "28409319-3daa-420b-8c1c-0044b4d4d6c4" {:text "Comment2"}}
                                                       :pages (sorted-map
                                                         "d429c2e1-f2b7-4d4f-abff-42eea0f8dc88" {
                                                           :title "My awesome page"
                                                           :author "Bobby Tables"

                                                           :width 640
                                                           :height 1080

                                                           ;:drawing {:shape :rectangle :x 100 :y 100}

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
                                                                    "gid3" {:name "Cross" :order 3 :visible true  :locked false :icon :line   :shapes ["id3" "id4"]}}})}
               "7b16847f-9298-4397-b093-a5364fdd1e97" {:name "Wireframes Taiga Tribe"
                                                       :uuid "7b16847f-9298-4397-b093-a5364fdd1e97"
                                                       :last-update (js/Date. 2005 10 1)
                                                       :created (js/Date. 2005 9 1)
                                                       :owner "user-1"
                                                       :comments {
                                                         "b1cafec8-4b1b-49b0-8ae2-3342dcbaaf6d" {:text "Comment1"}
                                                         "caee3210-4d16-4eae-a42f-a59a9228b886" {:text "Comment2"}
                                                         "8f017228-fecd-4145-ae92-c1c64b587b46" {:text "Comment1"}}
                                                       :pages (sorted-map
                                                         "082e1921-908d-4af6-897c-0a8a24d00b9b" {
                                                           :title "My awesome page"
                                                           :author "Bobby Tables"

                                                           :width 640
                                                           :height 1080

                                                           ;:drawing {:shape :rectangle :x 100 :y 100}

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
                                                                    "gid3" {:name "Cross" :order 3 :visible true  :locked false :icon :line   :shapes ["id3" "id4"]}}})}
               "01764df1-a6d6-407c-96c4-29110deeb641" {:name "A WYSH Roadmap"
                                                       :uuid "01764df1-a6d6-407c-96c4-29110deeb641"
                                                       :last-update (js/Date. 2010 10 1)
                                                       :created (js/Date. 2010 9 1)
                                                       :owner "user-2"
                                                       :comment []
                                                       :pages (sorted-map
                                                         "e654dff7-ef99-465d-949e-abb907dc69ce" {
                                                           :title "My awesome page"
                                                           :author "Bobby Tables"

                                                           :width 640
                                                           :height 1080

                                                           ;:drawing {:shape :rectangle :x 100 :y 100}

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
                                                                    "gid3" {:name "Cross" :order 3 :visible true  :locked false :icon :line   :shapes ["id3" "id4"]}}})}})

(defonce data (local-storage (atom {:users users :projects projects}) :data))

(defn get-projects
      [username]
      (let [projects (filter #(= (:owner %1) username) (vals (:projects @data)))]
        (map (fn [project]
               {:name (:name project)
                :uuid (:uuid project)
                :last-update (:last-update project)
                :created (:created project)
                :comment-count (count (:comments project))
                :first-page-uuid (first (keys (:pages project)))
                :pages (count (:pages project))}) projects)))

(defn get-project
      [uuid]
      (let [project (get-in @data [:projects uuid])]
        {:name (:name project)
         :uuid (:uuid project)
         :last-update (:last-update project)
         :created (:created project)
         :owner (:owner project)
         :comment-count (count (:comments project))
         :pages-count (count (:pages project))}))

(defn get-page
      [project-uuid page-uuid]
      (get-in @data [:projects project-uuid :pages page-uuid]))

