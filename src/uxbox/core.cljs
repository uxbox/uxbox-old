(ns ^:figwheel-always uxbox.core
    (:require
     [uxbox.ui :as ui]
     [uxbox.data.db :as db]
     [uxbox.navigation :as nav]
     [hodgepodge.core :refer [local-storage]]))

(enable-console-print!)

(def $el (.getElementById js/document "app"))

(defonce dev-storage (transient {}))

(defn start!
  [location]
  (let [conn (db/create)
        storage dev-storage]
    (nav/start-history!)
    (db/init! conn storage)
    (ui/render! $el location conn)))

(start! nav/location)
