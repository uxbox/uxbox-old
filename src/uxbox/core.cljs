(ns ^:figwheel-always uxbox.core
    (:require
     [uxbox.ui :as ui]
     [uxbox.ui.navigation :as nav]
     [uxbox.data.db :as db]
     [uxbox.data.schema :as sch]
     [hodgepodge.core :refer [local-storage]]))

(enable-console-print!)

(def $el (.getElementById js/document "app"))

(defn start!
  [location]
  (let [conn (db/create sch/schema)
        storage local-storage]
    (nav/start-history!)
    (db/init! conn storage)
    (ui/render! $el location conn)))

(start! nav/location)
