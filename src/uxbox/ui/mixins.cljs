(ns uxbox.ui.mixins
  (:require [rum]
            [datascript :as d]))

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
            query! (fn [db]
                     (let [eids (query db)]
                       (cond
                         (sequential? eids)
                         (d/pull-many db pull eids)

                         (not (nil? eids))
                         (d/pull db pull eids))))
            local-state (atom (query! @conn))
            component   (:rum/react-component state)]
        ;; sub
        (d/listen! conn
                   key
                   (fn [tx-report]
                     (when-let [r (query! (:db-after tx-report))]
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
