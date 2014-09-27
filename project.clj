(defproject phaser-cljs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]}]}
  )

;;; TODO: Must include "phaser.min.js"
