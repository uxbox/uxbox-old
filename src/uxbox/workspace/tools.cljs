(ns uxbox.workspace.tools)

;; Drawing tools

(defonce drawing-tools (atom {}))

;; Drawing tool
;; - shape: Type
;; - new: Component
;; - drawing: Component
;; - key
;; - icon
;; - text
;; - menu
;; - priority

(defn register-drawing-tool!
  [drawing-tool]
  (swap! drawing-tools assoc (:key drawing-tool) drawing-tool))

(defn sorted-tools
  [tools]
  (sort :priority (vals tools)))

;; Icon sets

(defonce icon-sets (atom {}))

(defn register-icon-set!
  [icon-set]
  (swap! icon-sets assoc (:key icon-set) icon-set))
