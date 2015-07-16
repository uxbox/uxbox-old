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

(pubsub/register-transition
 :close-lightbox
 (fn [state _]
   (assoc state :lightbox nil)))

(defn toggle-layout
  []
  (pubsub/publish! [:toggle-layout]))

(pubsub/register-transition
  :toggle-layout
  (fn [state _]
    (let [old-width (get-in state [:new-project :width])
          old-height (get-in state  [:new-project :height])]
      (-> state
        (assoc-in [:new-project :width] old-height)
        (assoc-in [:new-project :height] old-width)))))

(defn update-project-layout [state layout]
  (let [width (get-in state [:project-layouts layout :width])
        height (get-in state [:project-layouts layout :height])]
    (-> state
      (assoc-in [:new-project :width] width)
      (assoc-in [:new-project :height] height)
      (assoc-in [:new-project :layout] layout))))

(defn generate-layout-input [db layout]
  (let [human-name (get-in @db [:project-layouts layout :name])
        name (str layout)
        id (str "project-" name)
        tag (str "input#" id)
        tag (keyword tag)]
    [
      [tag
       {:type "radio"
        :name "project-layout"
        :value name
        :checked (= layout (get-in @db [:new-project :layout]))
        :on-change #(swap! db update-project-layout layout)}]
      [:label
        {:value name
         :for id}
         human-name]]))

(defn lightbox*
  [db]
  (let [tag (if (:lightbox @db) ;; TODO: select lightbox form depending on this value
              :div.lightbox
              :div.lightbox.hide)
        project-name (get-in @db [:new-project :name])
        project-width (get-in @db [:new-project :width])
        project-height (get-in @db [:new-project :height])
        project-layout (get-in @db [:new-project :layout])
        layouts (keys (:project-layouts @db))]
    [tag
       [:div.lightbox-body
        [:h3 "New project"]
        [:input#project-name.input-text
         {:placeholder "New project name"
          :type "text"
          :value project-name
          :on-change #(swap! db assoc-in [:new-project :name] (.-value (.-target %)))}]
        [:div.project-size
          [:input#project-witdh.input-text
           {:placeholder "Width"
            :type "number"
            :min 0 ;;TODO check this value
            :max 666666 ;;TODO check this value
            :value project-width
            :on-change #(swap! db assoc-in [:new-project :width] (.-value (.-target %)))}]
          [:a.toggle-layout
            {:href "#"
             :on-click #(toggle-layout)}
            icons/toggle]
          [:input#project-height.input-text
           {:placeholder "Height"
            :type "number"
            :min 0 ;;TODO check this value
            :max 666666 ;;TODO check this value
            :value project-height
            :on-change #(swap! db assoc-in [:new-project :height] (.-value (.-target %)))}]]
        (vec (cons :div.input-radio.radio-primary (mapcat #(generate-layout-input db %) layouts)))

        (when (not (empty? (trim project-name)))
          [:input#project-btn.btn-primary
            {:value "Go go go!"
             :type "button"
             :on-click #(let [new-project-attributes {:name (trim project-name)
                                                      :width (int project-width)
                                                      :height (int project-height)
                                                      :layout project-layout}]
                          (create-project new-project-attributes)
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
