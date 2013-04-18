(ns nightbyday.viewport
  (:use [clojure.test]))

(defn between [min x max]
  (Math/min (Math/max min x) max))

(defn limit-viewport [viewport layer]
  (let [{[layer-width layer-height] :size} layer
        [vx vy vw vh] viewport
        nvx (between 0 vx (- layer-width vw))
        nvx (if (> vw layer-width) (- (/ (- vw layer-width) 2)) nvx)
        nvy (between 0 vy (- layer-height vh))
        nvy (if (> vh layer-height) (- (/ (- vh layer-height) 2)) nvy)
        ]
    [nvx nvy vw vh]))

(deftest limit-viewport-test
  (let [layer {:size [2000 1000]}]
    (testing "when viewport fits inside the layer"
      (let [viewport-within-layer [0 0 1000 600]
            another-viewport-within-layer [50 60 1000 600]
            viewport-is-layer-wide [0 0 2000 600]]
        (testing "it stays the same"
          (is (= viewport-within-layer (limit-viewport viewport-within-layer layer)))
          (is (= another-viewport-within-layer (limit-viewport another-viewport-within-layer layer)))
          (is (= viewport-is-layer-wide (limit-viewport viewport-is-layer-wide layer)))
          )))
    (testing "when viewport is outside layer boundaries"
      (let [viewport-to-upper-left [-50 -60 1000 600]
            viewport-to-lower-right [1500 500 1000 600]]
        (testing "it is moved to within boundaries"
          (is (= [0 0 1000 600] (limit-viewport viewport-to-upper-left layer)))
          (is (= [1000 400 1000 600] (limit-viewport viewport-to-lower-right layer))))))
    (testing "when viewport is too large for layer"
      (let [viewport-too-wide [0 0 4000 600]
            viewport-too-wide-centered [-1000 0 4000 600]
            viewport-too-tall [0 0 1000 1200]
            viewport-too-tall-centered [0 -100 1000 1200]]
        (testing "it is centered"
          (is (= viewport-too-wide-centered (limit-viewport viewport-too-wide layer)))
          (is (= viewport-too-tall-centered (limit-viewport viewport-too-tall layer))))))
    ))

(run-tests)