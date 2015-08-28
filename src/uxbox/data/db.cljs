(ns uxbox.data.db
  (:require [uxbox.data.schema :as sch]
            [datascript :as d]))

(def conn (d/create-conn sch/schema))

;; TODO: restore from local storage?
