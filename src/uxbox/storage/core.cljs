(ns uxbox.storage.core
  (:require [hodgepodge.core :refer [local-storage set-item get-item]]
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

(defn start-storage! []
  (if (:data local-storage)
    (reset! atoms/storage (:data local-storage)))
  (add-watch atoms/storage :local-storage (fn [_ _ _ new-value] (assoc! local-storage :data new-value)))
  (add-watch atoms/storage :data-views-generator
     (fn [_ _ _ newValue] (proccess-event(last newValue))))

  (if (:projects-view local-storage)
    (reset! atoms/projects-view (:projects-view local-storage)))
  (add-watch atoms/projects-view :projects-view-storage (fn [_ _ _ new-value] (assoc! local-storage :projects-view new-value)))

  (if (:pages-view local-storage)
    (reset! atoms/pages-view (:pages-view local-storage)))
  (add-watch atoms/pages-view :pages-view-storage (fn [_ _ _ new-value] (assoc! local-storage :pages-view new-value)))

  (if (:activity-view local-storage)
    (reset! atoms/activity-view (:activity-view local-storage)))
  (add-watch atoms/activity-view :activity-view-storage (fn [_ _ _ new-value] (assoc! local-storage :activity-view new-value)))

  (if (:groups-view local-storage)
    (reset! atoms/groups-view (:groups-view local-storage)))
  (add-watch atoms/groups-view :groups-view-storage (fn [_ _ _ new-value] (assoc! local-storage :groups-view new-value)))

  (if (:shapes-view local-storage)
    (reset! atoms/shapes-view (:shapes-view local-storage)))
  (add-watch atoms/shapes-view :shapes-view-storage (fn [_ _ _ new-value] (assoc! local-storage :shapes-view new-value))))
