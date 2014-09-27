(ns phaser-cljs.core
  (:require-macros [phaser-cljs.macros :as m]))

(def game (atom nil))

(def screen-w (atom 0))
(def screen-h (atom 0))
(defn get-aspect-ratio []
  (/ @screen-w @screen-h))


(defn init! [logical-w logical-h target-id]
  (let [g (js/Phaser.Game logical-w logical-h Phaser.CANVAS target-id nil false false)]
    (reset! screen-w logical-w)
    (reset! screen-h logical-h)
    (reset! game g)
    nil))


(defn start-state! [k]
  (-> @game .state (.start (name k))))


(defn add-state!
  "Usage: (add-state! :state-name {:preload #(...), :create #(...), :update #(...)})"
  [k m]
  (-> @game .state (.add (name k) (clj->js m))))
