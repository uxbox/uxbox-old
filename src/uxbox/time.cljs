(ns uxbox.time
  (:require cljsjs.moment))

(defn ago [time]
  (.fromNow (js/moment time)))
