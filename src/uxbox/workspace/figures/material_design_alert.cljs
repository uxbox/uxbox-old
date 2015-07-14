(ns uxbox.workspace.figures.material-design-alert)

(def material-design-alert {
  :name "Material Design (Alert)"
  :symbols (sorted-map
    :error {
      :name "Error"
      :svg [:path {:xmlns "http://www.w3.org/2000/svg" :d "M24 4c-11.04 0-20 8.95-20 20s8.96 20 20 20 20-8.95 20-20-8.96-20-20-20zm2 30h-4v-4h4v4zm0-8h-4v-12h4v12z" :style {:stroke nil}}]}
    :warning {
      :name "Warning"
      :svg [:path {:xmlns "http://www.w3.org/2000/svg" :d "M2 42h44l-22-38-22 38zm24-6h-4v-4h4v4zm0-8h-4v-8h4v8z" :style {:stroke nil}}]})})
