(ns uxbox.storage.views
  (:require [hodgepodge.core :refer [local-storage set-item get-item]]))

(defonce projects-view
  (if (:projects-view local-storage)
    (atom (:projects-view local-storage))
    (atom {})))

(add-watch projects-view :local-storage (fn [_ _ _ new-value] (assoc! local-storage :projects-view new-value)))

(defonce pages-view
  (if (:pages-view local-storage)
    (atom (:pages-view local-storage))
    (atom {})))

(add-watch pages-view :local-storage (fn [_ _ _ new-value] (assoc! local-storage :pages-view new-value)))

(defonce activity-view
  (if (:activity-view local-storage)
    (atom (:activity-view local-storage))
    (atom {})))

(add-watch activity-view :local-storage (fn [_ _ _ new-value] (assoc! local-storage :activity-view new-value)))

