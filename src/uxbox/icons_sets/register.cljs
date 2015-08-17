(ns uxbox.icons-sets.register
  (:require [uxbox.pubsub :as pubsub]))

(pubsub/register-transition
 :register-icons-set
 (fn [state icons-set-info]
   (assoc-in state [:components :icons-sets (:key icons-set-info)] icons-set-info)))
