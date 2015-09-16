(ns uxbox.ui.tools)

;; TODO: Drawing tools and icon sets as data structures, put them in the DB

(defonce drawing-tools (atom {}))

(defn register-drawing-tool!
  [drawing-tool]
  (swap! drawing-tools assoc (:key drawing-tool) drawing-tool))

;; TODO: move to canvas.core when drawing tools are pure data,
;; drawing will be based on canvas protocols.
(defmulti start-drawing (fn [drawing-tool coords]
                          (first drawing-tool)))

(defn sorted-tools
  [tools]
  (sort :priority (vals tools)))

(defn get-tool
  [key]
  (get @drawing-tools key))

;; Icon sets

(defonce icon-sets (atom {}))

(defn register-icon-set!
  [icon-set]
  (swap! icon-sets assoc (:key icon-set) icon-set))
