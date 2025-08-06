(defproject invaders "0.1.0-SNAPSHOT"
  :description "Invaders game"
  :dependencies [[org.clojure/clojure "1.12.1"]
                 ;; Fancy terminal printing
                 [io.github.paintparty/bling "0.8.8"]]
  :repl-options {:init-ns invaders.core}
  :main invaders.core)
