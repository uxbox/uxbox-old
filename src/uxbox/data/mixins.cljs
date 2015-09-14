(ns uxbox.data.mixins
  (:require [rum]
            [datascript :as d]))

(defn query
  [query key]
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
