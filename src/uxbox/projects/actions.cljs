(ns uxbox.projects.actions
  (:require
   [uxbox.log.core :as log]
   [uxbox.projects.data :as d]))

(declare create-page)

(defn create-project
  [{:keys [name width height layout]}]
  (let [puuid (random-uuid)
        project (d/create-project puuid name width height layout)]
    (log/record! :uxbox/create-project project)
    (create-page project "Homepage")))

(defn create-page
  [project title]
  (let [puuid (random-uuid)
        page (d/create-page puuid
                            (:project/uuid project)
                            title
                            (:project/width project)
                            (:project/height project))]
    (log/record! :uxbox/create-page page)))

(defn change-page-title
  [page title]
  (log/record! :uxbox/change-page-title [page title]))

(defn delete-page
  [page]
  (log/record! :uxbox/delete-page (:page/uuid page)))

(defn delete-project
  [uuid]
  (log/record! :uxbox/delete-project uuid))
