(ns uxbox.forms.data)

(defonce lightbox (atom nil))

(defn set-lightbox!
  [kind]
  (reset! lightbox kind))

(defn close-lightbox!
  []
  (reset! lightbox nil))
