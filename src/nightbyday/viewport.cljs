(ns nightbyday.viewport)

(defn between [min x max]
  (Math/min (Math/max min x) max))

(defn limit-viewport [viewport layer]
  (let [{[layer-width layer-height] :size} layer
        [vx vy vw vh] viewport
        nvx (between 0 vx (- layer-width vw))
        nvx (if (> vw layer-width) (- (/ (- vw layer-width) 2)) nvx)        
        vw (if (> vw layer-width) layer-width vw)
        nvy (between 0 vy (- layer-height vh))
        nvy (if (> vh layer-height) (- (/ (- vh layer-height) 2)) nvy)
        vh (if (> vh layer-height) layer-height vh)
        ]
    [nvx nvy vw vh]))

