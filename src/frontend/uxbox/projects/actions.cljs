(ns uxbox.projects.actions
  (:require
   [uxbox.log.core :as log]
   [uxbox.projects.data :as d]))

(declare create-page)

(defn create-project
  [conn {:keys [name width height layout]}]
  (let [puuid (random-uuid)
        project (d/create-project puuid name width height layout)]
    (log/record! conn :uxbox/create-project project)
    (create-page conn project "Homepage")))

(defn create-page
  [conn project title]
  (let [puuid (random-uuid)
        page (d/create-page puuid
                            (:project/uuid project)
                            title
                            (:project/width project)
                            (:project/height project))]
    (log/record! conn :uxbox/create-page page)))

(defn change-page-title
  [conn page title]
  (log/record! conn :uxbox/change-page-title [page title]))

(defn delete-page
  [conn page]
  (log/record! conn :uxbox/delete-page (:page/uuid page)))

(defn delete-project
  [conn uuid]
  (log/record! conn :uxbox/delete-project uuid))
