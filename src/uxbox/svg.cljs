(ns uxbox.svg)

(defn translate
  [x y]
  (str "translate(" x "," y ")"))

(defn scale
  [x y]
  (str "scale(" x "," y ")"))

(defn rotate
  [d]
  (str "rotate(" d ")"))

(defn transform
  [{:keys [center] :as opts}]
  (let [r (get opts :rotate 0)
        {:keys [x y]} center]
    (str (translate x y)
         (rotate r)
         (translate (- x) (- y)))))
