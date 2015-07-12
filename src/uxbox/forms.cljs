(ns uxbox.forms
  (:require [uxbox.projects.actions :refer [create-project]]
            [uxbox.icons :as icons]
            [uxbox.pubsub :as pubsub]
            [goog.events :as events]
            [cuerdas.core :refer [trim]])
  (:import goog.events.EventType))

(defn close-lightbox
  []
  (pubsub/publish! [:close-lightbox]))

(pubsub/register-handler
 :close-lightbox
 (fn [state _]
   (assoc state :lightbox nil :new-project-name "")))

(defn lightbox*
  [db]
  (let [tag (if (:lightbox @db) ;; TODO: select lightbox form depending on this value
              :div.lightbox
              :div.lightbox.hide)
        project-name (:new-project-name @db)]
    [tag
     [:div.lightbox-body
      [:h3 "New project"]
      [:input#project-name.input-text
       {:placeholder "New project name"
        :type "text"
        :value project-name
        :on-change #(swap! db assoc :new-project-name (.-value (.-target %)))}]
      (when (not (empty? (trim project-name)))
        [:input#project-btn.btn-primary
          {:value "Go go go!"
           :type "button"
           :on-click #(do
                        (create-project (trim project-name))
                        (close-lightbox))}])
      [:a.close
       {:href "#"
        :on-click #(close-lightbox)}
       icons/close]]]))

(defn dismiss-lightbox
  [e]
  (when (= (.-keyCode e) 27)
    (close-lightbox)))

;; NOTE: alter-meta! does not work on vars http://dev.clojure.org/jira/browse/CLJS-1248
(def lightbox
  (with-meta lightbox* {:component-did-mount #(events/listen js/document EventType.KEYUP dismiss-lightbox)
                        :component-will-unmount #(events/unlisten js/document EventType.KEYUP dismiss-lightbox)}))
