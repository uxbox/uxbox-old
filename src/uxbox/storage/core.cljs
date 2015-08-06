(ns uxbox.storage.core
  (:require [hodgepodge.core :refer [local-storage set-item get-item]]
            [uxbox.storage.generators :as generators]))

(defn proccess-event [event]
  ;; TODO
  ;; (generators/undo-tree event)
  (generators/projects-data event)
  (generators/pages-data event)
  event)

(defonce storage
  (if (:data local-storage)
    (atom (:data local-storage))
    (atom [])))

(add-watch storage :local-storage (fn [_ _ _ new-value] (assoc! local-storage :data new-value)))

(defn insert-event [event-data]
  (let [now (js/Date.)
        event (assoc event-data :now now)]
    (swap! storage (fn [current] (conj current event)))))

;; TODO
;; (add-watch storage (fn [at oldValue newValue] (generators/undo-tree newValue)))

(add-watch storage :data-views-generator
   (fn [key atom oldValue newValue]
    (proccess-event(last newValue))))
