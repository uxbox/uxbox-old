(ns uxbox.storage.core
  (:require
    [uxbox.storage.backend :as backend]
    [uxbox.storage.generators :as generators]
    [uxbox.storage.atoms :as atoms]))

(defn proccess-event [event]
  ;; TODO
  ;; (generators/undo-tree event)
  (generators/projects-data event)
  (generators/pages-data event)
  (generators/groups-data event)
  (generators/shapes-data event)
  event)

(defn insert-event [event-data]
  (let [now (js/Date.)
        event (assoc event-data :now now)]
    (swap! atoms/storage (fn [current] (conj current event)))))

(defn start-storage!
  ([]
   (start-storage! backend/default-backend))
  ([b]
   (backend/set-backend! b)
   (add-watch atoms/storage
              :data-views-generator
              (fn [_ _ _ new-value]
                (proccess-event (last new-value))))
   (backend/bind-key atoms/storage :data)
   (backend/bind-key atoms/projects-view :projects-view)
   (backend/bind-key atoms/pages-view :pages-view)
   (backend/bind-key atoms/activity-view :activity-view)
   (backend/bind-key atoms/groups-view :groups-view)
   (backend/bind-key atoms/shapes-view :shapes-view)))
