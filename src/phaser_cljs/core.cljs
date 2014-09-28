(ns phaser-cljs.core)



(def dom-id (atom nil))
(def game (atom nil))

(def screen-w (atom 0))
(def screen-h (atom 0))
(def aspect-ratio (atom 0))

(def asset-dir (atom "."))
(defn asset-path [filename]
  (str @asset-dir "/" filename))



;;;
;;; Initialization
;;;

(defn init! [logical-w logical-h target-id asset-dir-path]
  (let [g (js/Phaser.Game. logical-w logical-h Phaser.CANVAS target-id nil false false)]
    (reset! dom-id target-id)
    (reset! screen-w logical-w)
    (reset! screen-h logical-h)
    (reset! game g)
    (reset! aspect-ratio (/ logical-w logical-h))
    (reset! asset-dir asset-dir-path)
    nil))


(defn- calc-screen [new-screen-w new-screen-h]
  (let [current-aspect-ratio (/ new-screen-w new-screen-h)]
    (if (= @aspect-ratio current-aspect-ratio)
      [new-screen-w new-screen-h 0 0]
      (if (< @aspect-ratio current-aspect-ratio)
        (let [w (* new-screen-h @aspect-ratio)
              h new-screen-h
              left (/ (- new-screen-w w) 2)
              top 0]
          [w h left top])
        (let [w new-screen-w
              h (/ new-screen-w @aspect-ratio)
              left 0
              top (/ (- new-screen-h h) 2)]
          [w h left top])))))

(defn set-resize-handler! []
  (let [;; EXACT_FIT or NO_SCALE or RESIZE or SHOW_ALL
        mode js/Phaser.ScaleManager.SHOW_ALL
        handle (fn []
                 (let [dom (js/document.getElementById @dom-id)
                       ;; NB: Can get margin/padding
                       new-screen-w js/document.documentElement.clientWidth
                       new-screen-h js/document.documentElement.clientHeight
                       [w h left top] (calc-screen new-screen-w new-screen-h)]
                   (set! dom.style.marginLeft (str left "px"))
                   (set! dom.style.marginTop (str top "px"))
                   (set! (.-width @game) @screen-w)
                   (set! (.-height @game) @screen-h)
                   (set! (.-width (.-canvas @game)) w)
                   (set! (.-height (.-canvas @game)) h)
                   (-> @game .-world (.setBounds 0 0 @screen-w @screen-h))
                   (set! (.-width (.-scale @game)) w)
                   (set! (.-height (.-scale @game)) h)
                   (-> @game .-renderer (.resize @screen-w @screen-h))
                   (-> @game .-scale (.setSize))
                   (-> @game .-camera (.setSize @screen-w @screen-h))
                   (-> @game .-camera (.setBoundsToWorld))
                   (js/Phaser.Canvas.setSmoothingEnabled
                     (-> @game .-context)
                     (-> @game .-antialias))
                   nil))]
    (set! (.-scaleMode (.-scale @game)) mode)
    (set! (.-onResize (.-scale @game)) handle)
    ;; Run once
    (handle)))


(defn disable-visibility-change! [bool]
  (set! (.-disableVisibilityChange (.-stage @game)) bool))


;;;
;;; State
;;;

(defn start-state! [k]
  (-> @game .-state (.start (name k))))

(defn add-state!
  "Usage: (add-state! :state-name {:preload #(...), :create #(...), :update #(...)})"
  [k m]
  (-> @game .-state (.add (name k) (clj->js m))))



;;;
;;; Loader
;;;

;;; TODO: Verify loaded keys when using

(defn load-audio! [k & files]
  (-> @game .-load (.audio (name k) (clj->js (map asset-path files)))))

(defn load-image! [k file]
  (-> @game .-load (.image (name k) (asset-path file))))

(defn load-spritesheet! [k file frame-w frame-h & [frame-max margin spacing]]
  (let [frame-max (or frame-max -1)
        margin (or margin 0)
        spacing (or spacing 0)]
    (-> @game .-load (.spritesheet (name k)
                                   (asset-path file)
                                   frame-w
                                   frame-h
                                   frame-max
                                   margin
                                   spacing))))




;;;
;;; Adder
;;;


(defn add-sprite! [k x y & [sprite-width sprite-height anchor-x anchor-y]]
  (let [anchor-x (or anchor-x 0.5)
        anchor-y (or anchor-y anchor-x)
        sp (-> @game .-add (.sprite x y (name k)))]
    (.setTo (.-anchor sp) anchor-x anchor-y)
    (when (and sprite-width)
      (set! (.-width sp) sprite-width))
    (when (and sprite-height)
      (set! (.-height sp) sprite-height))
    sp))

(defn add-text! [text x y & [style]]
  (let [style (merge {:font "16px monospace"
                      :fill "#FFFFFF"
                      :align "center"
                      } (or style {}))]
    (-> @game .-add (.text x y text (clj->js style)))))

(defn add-audio! [k]
  (-> @game .-add (.audio (name k))))


;;; This is heavy
(defn add-particle-emitter! [k]
  (doto (-> @game .-add (.emitter 0 0 50))
    (.makeParticles (name k))))

(defn emit-particle! [pe x y]
  (set! (.-x pe) x)
  (set! (.-y pe) y)
  (.start pe  true 500 nil 10))




;;;
;;; Input
;;;

(defn kbd [k]
  (aget js/Phaser.Keyboard (name k)))

(defn add-key-capture! [& ks]
  (-> @game .-input .-keyboard (.addKeyCapture (clj->js (map kbd ks)))))


(defn is-key-down? [k]
  (-> @game .-input .-keyboard (.isDown (kbd k))))




;;;
;;; Debug
;;;

(defn debug-text! [text x y]
  (-> @game .-debug (.text text x y)))










