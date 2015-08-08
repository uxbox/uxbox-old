(ns uxbox.storage.views
  (:require [hodgepodge.core :refer [local-storage set-item get-item]]))

(defonce projects-view
  (if (:projects-view local-storage)
    (atom (:projects-view local-storage))
    (atom {})))

(add-watch projects-view :projects-view-storage (fn [_ _ _ new-value] (assoc! local-storage :projects-view new-value)))

(defonce pages-view
  (if (:pages-view local-storage)
    (atom (:pages-view local-storage))
    (atom {})))

(add-watch pages-view :page-view-storage (fn [_ _ _ new-value] (assoc! local-storage :pages-view new-value)))

(defonce activity-view
  (if (:activity-view local-storage)
    (atom (:activity-view local-storage))
    (atom {})))

(add-watch activity-view :activity-view-storage (fn [_ _ _ new-value] (assoc! local-storage :activity-view new-value)))

(defonce groups-view
  (if (:groups-view local-storage)
    (atom (:groups-view local-storage))
    (atom {})))

(add-watch groups-view :groups-view-storage (fn [_ _ _ new-value] (assoc! local-storage :groups-view new-value)))

(defonce shapes-view
  (if (:shapes-view local-storage)
    (atom (:shapes-view local-storage))
    (atom {})))

(add-watch shapes-view :shapes-view-storage (fn [_ _ _ new-value] (assoc! local-storage :shapes-view new-value)))
