(ns uxbox.storage.backend)

(defonce default-backend (transient {}))

(defonce backend (atom default-backend))

(defn bind-key
  [at key]
  (let [data (get @backend key @at)]
    (reset! at data))

  (add-watch at key (fn [_ _ _ new-value]
                      (swap! backend assoc! key new-value)))

  (add-watch backend key (fn [_ _ old-backend new-backend]
                           (if (not= old-backend new-backend)
                             (when-let [data (get new-backend key)]
                               (reset! at data)))))
   at)

(defn set-backend!
  [b]
  (reset! backend b))
