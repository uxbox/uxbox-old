(ns uxbox.storage
  (:require [hodgepodge.core :refer [local-storage set-item get-item]]
            [uxbox.shapes.core :refer [Rectangle Line move-delta]]))

(def users [{:username "user-1"
             :password "user-1"}
            {:username "user-2"
             :password "user-2"}])

(def projects {(uuid "c078f148-2686-4b07-a165-c455a0ab19a7") {:name "Design UX Box"
                                                       :uuid (uuid "c078f148-2686-4b07-a165-c455a0ab19a7")
                                                       :last-update (js/Date. 2014 10 1)
                                                       :created (js/Date. 2014 9 1)
                                                       :owner "user-1"
                                                       :comments {
                                                         "3e14aaed-e3ab-45f4-9fc5-14dc023a545e" {:text "Comment1"}
                                                         "28409319-3daa-420b-8c1c-0044b4d4d6c4" {:text "Comment2"}}
                                                       :pages {
                                                         (uuid "d429c2e1-f2b7-4d4f-abff-42eea0f8dc88") {
                                                           :title "My awesome page"
                                                           :uuid (uuid "d429c2e1-f2b7-4d4f-abff-42eea0f8dc88")
                                                           :author "Bobby Tables"

                                                           :width 640
                                                           :height 1080

                                                           ;:drawing {:shape :rectangle :x 100 :y 100}

                                                           :shapes {}
                                                           :groups {}}
                                                          (uuid "3df37adb-f18d-4afb-9908-397e3d46653d") {
                                                            :title "Another awesome page"
                                                            :uuid (uuid "3df37adb-f18d-4afb-9908-397e3d46653d")
                                                            :author "Bobby Tables"

                                                            :width 640
                                                            :height 1080
                                                           :shapes {}
                                                           :groups {}}}}
               (uuid "7b16847f-9298-4397-b093-a5364fdd1e97") {:name "Wireframes Taiga Tribe"
                                                       :uuid (uuid "7b16847f-9298-4397-b093-a5364fdd1e97")
                                                       :last-update (js/Date. 2005 10 1)
                                                       :created (js/Date. 2005 9 1)
                                                       :owner "user-1"
                                                       :comments {
                                                         "b1cafec8-4b1b-49b0-8ae2-3342dcbaaf6d" {:text "Comment1"}
                                                         "caee3210-4d16-4eae-a42f-a59a9228b886" {:text "Comment2"}
                                                         "8f017228-fecd-4145-ae92-c1c64b587b46" {:text "Comment1"}}
                                                       :pages {
                                                         (uuid "082e1921-908d-4af6-897c-0a8a24d00b9b") {
                                                           :title "My awesome page"
                                                           :uuid (uuid "082e1921-908d-4af6-897c-0a8a24d00b9b")
                                                           :author "Bobby Tables"

                                                           :width 640
                                                           :height 1080

                                                           ;:drawing {:shape :rectangle :x 100 :y 100}

                                                           :shapes {}

                                                           :groups {}}}}
               (uuid "01764df1-a6d6-407c-96c4-29110deeb641") {:name "A WYSH Roadmap"
                                                       :uuid (uuid "01764df1-a6d6-407c-96c4-29110deeb641")
                                                       :last-update (js/Date. 2010 10 1)
                                                       :created (js/Date. 2010 9 1)
                                                       :owner "user-2"
                                                       :comment []
                                                       :pages {
                                                         (uuid "e654dff7-ef99-465d-949e-abb907dc69ce") {
                                                           :title "My awesome page"
                                                           :uuid (uuid "e654dff7-ef99-465d-949e-abb907dc69ce")
                                                           :author "Bobby Tables"

                                                           :width 640
                                                           :height 1080

                                                           ;:drawing {:shape :rectangle :x 100 :y 100}

                                                           :shapes {}

                                                           :groups {}}}}})

(defonce data
  (if (:data local-storage)
    (atom (:data local-storage))
    (atom {:users users :projects projects})))

(add-watch data :local-storage (fn [_ _ _ new-value] (assoc! local-storage :data new-value)))

(defn get-projects
      [username]
      (into {} (map (fn [project]
        [(:uuid project)
         {:name (:name project)
          :uuid (:uuid project)
          :last-update (:last-update project)
          :created (:created project)
          :comment-count (count (:comments project))
          :first-page-uuid (first (keys (:pages project)))
          :pages (count (:pages project))
          :width (:width project)
          :height (:height project)
          :layout (:layout project)
          :pages-count (count (:pages project))}]) (vals (:projects @data)))))

(defn get-project
      [uuid]
      (let [project (get-in @data [:projects uuid])]
        {:name (:name project)
         :uuid (:uuid project)
         :last-update (:last-update project)
         :created (:created project)
         :owner (:owner project)
         :comment-count (count (:comments project))
         :pages-count (count (:pages project))
         :width (:width project)
         :height (:height project)
         :layout (:layout project)
         :pages (:pages project)}))

(defn create-project
      [project]
      (swap! data (fn [current] (assoc-in current [:projects (:uuid project)] project))))

(defn create-page
      [project-uuid page]
      (swap! data (fn [current] (assoc-in current [:projects project-uuid :pages (:uuid page)] page))))

(defn change-page-title
      [project-uuid page title]
      (swap! data (fn [current] (assoc-in current [:projects project-uuid :pages (:uuid page) :title] title))))

(defn delete-page
      [project-uuid page]
      (swap! data (fn [current] (update-in current [:projects project-uuid :pages] dissoc (:uuid page)))))

(defn delete-project
      [uuid]
      (swap! data (fn [current] (update current :projects dissoc uuid))))

(defn get-page
      [project-uuid page-uuid]
      (get-in @data [:projects project-uuid :pages page-uuid]))

(defn create-shape
      [project-uuid page-uuid shape-uuid shape]
      (swap! data (fn [current] (assoc-in current [:projects project-uuid :pages page-uuid :shapes shape-uuid] shape))))

(defn remove-element [groups-entry element-uuid]
  (let [in? (fn [seq elm] (some #(= elm %) seq))
        has-element? (fn [[_ val]] (in? (:shapes val) element-uuid)  )
        owner-uuid (->> groups-entry (filter has-element?) first first)
        remove-vector-element (fn [v el] (vector (filter #(not (= % el)) v)))]
    (cond
      (nil? owner-uuid) groups-entry
      (= 1 (-> groups-entry (get owner-uuid) :shapes count)) (dissoc groups-entry owner-uuid)
      :else (update-in groups-entry [:shapes] remove-vector-element element-uuid)
      )))

(defn remove-shape
      [project-uuid page-uuid shape-uuid]
      (swap! data (fn [current] (-> current
                                    (update-in [:projects project-uuid :pages page-uuid :groups] remove-element shape-uuid)
                                    (update-in [:projects project-uuid :pages page-uuid :shapes] dissoc shape-uuid)))))

(defn create-group
      [project-uuid page-uuid group-uuid group]
      (swap! data (fn [current] (assoc-in current [:projects project-uuid :pages page-uuid :groups group-uuid] group))))

(defn change-shape-attr
      [project-uuid page-uuid shape-uuid attr value]
      (swap! data (fn [current] (assoc-in current [:projects project-uuid :pages page-uuid :shapes shape-uuid attr] value))))

(defn move-shape
      [project-uuid page-uuid shape-uuid deltax deltay]
      (swap! data (fn [current] (update-in current [:projects project-uuid :pages page-uuid :shapes shape-uuid] move-delta deltax deltay))))
