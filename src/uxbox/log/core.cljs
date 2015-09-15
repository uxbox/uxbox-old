(ns uxbox.log.core
  (:require
   [datascript :as d]
   [uxbox.data.db :as db]
   [uxbox.user.data :refer [user]]))

(def event-types #{:uxbox/create-project
                   :uxbox/delete-project
                   :uxbox/create-page
                   :uxbox/change-page-title
                   :uxbox/delete-page
                   :uxbox/create-shape
                   :uxbox/delete-shape
                   :uxbox/move-shape
                   :uxbox/change-shape
                   :uxbox/toggle-shape-visibility
                   :uxbox/toggle-shape-lock
                   ;; TODO
                   :uxbox/move-shape-up
                   :uxbox/move-shape-down})

;; to-datoms :: Event -> DB -> [Datom]
(defmulti to-datoms (fn [event db]
                      (:event/type event))
          :default :uxbox/no-op)

(defmethod to-datoms :uxbox/no-op [_ _] [])

;; materialize :: Event -> DB -> Event
(defmulti  materialize (fn [event db]
                         (:event/type event))
           :default :uxbox/no-op)

(defmethod materialize :uxbox/no-op [event _] event)

;; Type -> Payload -> Event
(defn event
  [type payload]
  (let [now (js/Date.)]
    {:event/type type
     :event/timestamp now
     :event/payload payload
     :event/author @user}))

;; needed to make possible to insert arbitrary maps in Datascript
(extend-protocol IComparable
  PersistentArrayMap
  (-compare [one other]
    (compare (vec one) (vec other)))

  PersistentHashMap
  (-compare [one other]
    (compare (vec one) (vec other))))

;; persist! :: Event -> Conn -> Conn
(defn persist!
  [conn ev]
  (let [ds (to-datoms ev @conn)]
    (d/transact! conn ds)))

(defn record!
  ([type payload]
   (record! type payload db/conn))
  ([conn type payload]
   (let [ev (event type payload)]
     (persist! conn ev)
     (d/transact! conn [ev]))))
