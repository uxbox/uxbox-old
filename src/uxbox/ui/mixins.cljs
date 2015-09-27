(ns uxbox.ui.mixins
  (:require
   [rum]
   [datascript :as d]
   [uxbox.queries :as qs]))

;; ================================================================================
;; Queries


(defn query
  [key query]
  { :transfer-state
    (fn [old new]
      (assoc new key (old key)))
    :will-mount
    (fn [state]
      (let [[conn] (:rum/args state)
            local-state (atom (query @conn))
            component   (:rum/react-component state)]
        ;; sub
        (d/listen! conn
                   key
                   (fn [tx-report]
                     (when-let [r (query (:db-after tx-report))]
                       (when (not= @local-state r)
                         (reset! local-state r)
                         (rum/request-render component)))))
        (assoc state key local-state)))
   :will-unmount
   (fn [state]
     (let [[conn] (:rum/args state)]
       ;; unsub
       (d/unlisten! conn key)
       (dissoc state key)))})

(defn pull-query
  [key query pull]
  { :transfer-state
    (fn [old new]
      (assoc new key (old key)))
    :will-mount
    (fn [state]
      (let [[conn] (:rum/args state)
            local-state (qs/rpull query pull conn)
            component   (:rum/react-component state)]
        ;; sub
        (add-watch local-state
                   key
                   (fn [_ _ old new]
                     (when-not (= old new)
                       (rum/request-render component))))
        (assoc state key local-state)))
   :will-unmount
   (fn [state]
     (let [[conn] (:rum/args state)]
       ;; unsub
       (remove-watch (state key) key)
       (dissoc state key)))})

;; ================================================================================
;; Commands

(defn- sub-all
  [cmds args]
  (let [subs (into [] (for [[key action eff :as cmd] cmds]
                        [key action]))]
    (doseq [[key action eff :as cmd] cmds]
      (add-watch action
                 key
                 (fn [_ _ _ v]
                   (eff args v))))
    subs))

(defn unsub-all
  [subs]
  (doseq [[skey action] subs]
    (remove-watch action skey)))

(defn cmds-mixin
  [& cmds]
  {:will-mount (fn [state]
                 (let [args (:rum/args state)
                       subs (sub-all cmds args)]
                   (assoc state ::cmds subs)))
   :transfer-state (fn [old new]
                     (let [args (:rum/args new)
                           [_ _ shapes] args
                           oldsubs (::cmds old)]
                       (unsub-all oldsubs)
                       (assoc new ::cmds (sub-all cmds args))))
   :wrap-render (fn [render-fn]
                  (fn [state]
                    (let [[dom next-state] (render-fn state)]
                      [dom (assoc next-state ::cmds (::cmds state))])))
   :will-unmount (fn [state]
                   (unsub-all (::cmds state))
                   (dissoc state ::cmds))})
