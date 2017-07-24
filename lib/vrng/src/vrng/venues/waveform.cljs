(ns vrng.venues.waveform)

(defn normalize-volume-points
  "applies normalization and caps volume at 1"
  [volumes]
  (map #(min 1 (/ (.pow js/Math % 1) 2)) volumes))

(defn volume->points
  "returns 2-tuples given volume data points"
  [volume width]
  (let [step-size (/ width (count volume))]
    (map-indexed #(vector (* %1 step-size) %2) volume)
    ))

(defn top-half
  "scales tuple y coordinates to create top half of waveform"
  [points height]
  (map #(assoc % 1 (+ (* (nth % 1) (/ height -2)) (/ height 2))) points))

(defn bottom-half
  "scales tuple y coordinates to create bottom half of waveform"
  [points height]
  (map #(assoc % 1 (+ (* (nth % 1) (/ height 2)) (/ height 2))) points))

(defn points->coords
  "returns actual svg coordinates given tuples"
  [points height]
  (concat (top-half points height) (reverse (bottom-half points height))))

(defn coords->polygon
  "returns svg polygon given points"
  [coords]
  [:polygon {:points (clojure.string/join " " (map #(clojure.string/join "," %) coords))}])

(defn volume->polygon
  "chains functions for convenience"
  [volume width height]
  (-> volume (volume->points width) (points->coords height) (coords->polygon)))


(defn waveform-svg
  "returns actual svg waveform component"
  [{left :left right :right duration :duration} width height]
  [:svg {:x 0 :y 0 :width width :height height}
   (let [volume-points (mapv + right left)]
     (volume->polygon (normalize-volume-points volume-points) width height))
   ])

(def json-test-string
  "{
  \"left\": [0.84,0.57,0.59,0.87,0.92,0.97,0.71,0.7,0.91,0.79,0.98,0.55,0.9,0.97,0.93,0.72,0.31,0.3,0.24,0.23,0.23,0.33,0.23,0.2,0.23,0.24,0.17,0.25,0.23,0.44,0.35,0.44,0.4,0.81,0.86,0.69,0.72,0.7,0.87,0.82,0.79,0.86,0.71,0.76,0.7,0.76,0.91,0.74,0.86,0.72,0.7,0.76,0.86,0.93,0.92,0.89,0.69,0.75,0.71,0.76,0.77,0.26,0.27,0.33,0.32,0.3,0.31,0.4,0.2,0.25,0.22,0.29,0.38,0.25,0.34,0.34,0.31,0.3,0.21,0.16,0.18,0.18,0.18,0.11,0.28,0.31,0.18,0.27,0.25,0.23,0.24,0.11,0.24,0.15,0.21,0.16,0.26,0.13,0.22,0.28,0.28,0.16,0.12,0.2,0.26,0.26,0.23,0.25,0.19,0.25,0.21,0.23,0.19,0.15,0.16,0.25,0.2,0.32,0.22,0.15,0.21,0.14,0.16,0.26,0.24,0.28,0.3,0.31,0.35,0.14,0.24,0.22,0.25,0.23,0.11,0.15,0.3,0.28,0.25,0.23,0.23,0.25,0.29,0.27,0.14,0.32,0.22,0.19,0.16,0.23,0.2,0.19,0.22,0.19,0.18,0.22,0.24,0.12,0.16,0.27,0.3,0.21,0.13,0.21,0.21,0.36,0.31,0.31,0.13,0.17,0.19,0.33,0.12,0.17,0.11,0.18,0.28,0.32,0.18,0.22,0.26,0.29,0.19,0.17,0.24,0.26,0.24,0.21,0.19,0.26,0.21,0.21,0.35,0.27,0.29,0.21,0.28,0.12,0.23,0.15,0.18,0.13,0.17,0.28,0.23,0.35,0.2,0.26,0.14,0.17,0.27,0.18,0.16,0.2,0.18,0.26,0.32,0.28,0.12,0.24,0.21,0.2,0.22,0.21,0.24,0.31,0.14,0.15,0.19,0.24,0.28,0.29,0.24,0.3,0.24,0.21,0.3,0.16,0.29,0.37,0.2,0.24,0.29,0.19,0.2,0.21,0.35,0.28,0.21,0.13,0.23,0.19,0.42,0.23,0.2,0.26,0.26,0.12,0.35,0.3,0.19,0.33,0.39,0.27,0.27,0.23,0.18,0.24,0.32,0.25,0.38,0.29,0.16,0.28,0.28,0.22,0.25,0.2,0.25,0.24,0.28,0.24,0.18,0.25,0.19,0.16,0.26,0.24,0.34,0.18,0.22,0.16,0.21,0.27,0.33,0.19,0.33,0.34,0.34,0.46,0.21,0.2,0.21,0.16,0.12,0.27,0.33,0.21,0.19,0.33,0.34,0.19,0.14,0.14,0.22,0.14,0.21,0.3,0.26,0.16,0.18,0.13,0.37,0.28,0.23,0.24,0.16,0.21,0.27,0.25,0.21,0.22,0.23,0.25,0.23,0.23,0.21,0.18,0.14,0.22,0.2,0.21,0.12,0.24,0.2,0.2,0.32,0.32,0.21,0.23,0.21,0.19,0.19,0.17,0.33,0.15,0.17,0.16,0.15,0.13,0.21,0.2,0.22,0.15,0.19,0.18,0.29,0.22,0.24,0.19,0.19,0.2,0.26,0.24,0.19,0.18,0.26,0.15,0.27,0.26,0.25,0.25,0.24,0.29,0.11,0.21,0.25,0.23,0.25,0.36,0.21,0.23,0.2,0.18,0.17,0.13,0.23,0.28,0.24,0.21,0.27,0.18,0.14,0.19,0.18,0.16,0.18,0.089,0.28,0.25,0.22,0.21,0.18,0.21,0.17,0.26,0.32,0.27,0.26,0.27,0.2,0.21,0.095,0.17,0.21,0.12,0.11,0.1,0.08,0.12,0.26,0.24,0.2,0.26,0.2,0.17,0.19,0.13,0.16,0.29,0.29,0.23,0.35,0.26,0.21,0.2,0.21,0.22,0.23,0.24,0.24,0.2,0.2,0.15,0.12,0.11,0.24,0.25,0.19,0.22,0.23,0.18,0.26,0.19,0.24,0.17,0.14,0.3,0.22,0.24,0.098,0.25,0.22,0.25,0.15,0.26,0.21,0.27,0.21,0.21,0.17,0.24,0.22,0.2,0.24,0.17,0.23,0.14,0.15,0.21,0.14,0.24,0.19,0.21,0.2,0.15,0.23,0.14,0.21,0.24,0.21,0.26,0.24,0.29,0.24,0.22,0.15,0.25,0.22,0.12,0.19,0.23,0.18,0.14,0.25,0.13,0.26,0.15,0.13,0.29,0.25,0.25,0.13,0.2,0.12,0.15,0.12,0.21,0.19,0.19,0.14,0.17,0.095,0.32,0.23,0.2,0.16,0.22,0.098,0.12,0.23,0.21,0.19,0.26,0.18,0.11,0.2,0.14,0.36,0.17,0.2,0.22,0.15,0.21,0.15,0.28,0.14,0.17,0.21,0.19,0.34,0.18,0.15,0.29,0.22,0.11,0.15,0.19,0.24,0.27,0.26,0.25,0.21,0.2,0.23,0.21,0.18,0.24,0.15,0.17,0.18,0.19,0.11,0.24,0.25,0.26,0.2,0.12,0.13,0.16,0.2,0.27,0.22,0.35,0.18,0.2,0.24,0.22,0.13,0.41,0.2,0.17,0.23,0.2,0.27,0.16,0.17,0.23,0.22,0.27,0.22,0.24,0.27,0.24,0.17,0.27,0.2,0.27,0.14,0.23,0.22,0.15,0.21,0.25,0.18,0.25,0.21,0.19,0.12,0.25,0.2,0.19,0.3,0.33,0.14,0.094,0.27,0.13,0.23,0.24,0.25,0.29,0.28,0.22,0.31,0.12,0.25,0.19,0.22,0.25,0.31,0.31,0.17,0.24,0.33,0.27,0.15,0.15,0.25,0.3,0.3,0.21,0.24,0.39,0.21,0.22,0.22,0.25,0.11,0.26,0.19,0.21,0.28,0.096,0.11,0.27,0.17,0.15,0.19,0.23,0.2,0.34,0.21,0.16,0.32,0.2,0.28,0.34,0.28,0.32,0.31,0.25,0.14,0.22,0.16,0.14,0.2,0.15,0.24,0.21,0.19,0.35,0.33,0.21,0.23,0.4,0.21,0.26,0.22,0.2,0.22,0.21,0.19,0.23,0.26,0.25,0.21,0.33,0.29,0.16,0.24,0.22,0.16,0.18,0.17,0.14,0.19,0.13,0.18,0.21,0.27,0.15,0.15,0.23,0.28,0.19,0.19,0.16,0.33,0.17,0.15,0.18,0.15,0.26,0.25,0.15,0.2,0.26,0.21,0.16,0.19,0.22,0.32,0.26,0.12,0.26,0.19,0.17,0.23,0.2,0.18,0.16,0.15,0.22,0.3,0.21,0.17,0.15,0.23,0.23,0.19,0.22,0.23,0.14,0.25,0.24,0.23,0.18,0.25,0.27,0.19,0.28,0.16,0.2,0.21,0.22,0.2,0.2,0.19,0.2,0.19,0.14,0.19,0.094,0.15,0.17,0.12,0.23,0.23,0.14],
  \"right\": [0.84,0.57,0.59,0.87,0.92,0.97,0.71,0.7,0.91,0.79,0.98,0.55,0.9,0.97,0.93,0.72,0.31,0.3,0.24,0.23,0.23,0.33,0.23,0.2,0.23,0.24,0.17,0.25,0.23,0.43,0.37,0.44,0.41,0.81,0.88,0.68,0.74,0.68,0.9,0.87,0.74,0.84,0.71,0.77,0.7,0.77,0.84,0.7,0.87,0.7,0.7,0.76,0.82,1,0.9,0.89,0.69,0.75,0.71,0.77,0.76,0.24,0.28,0.33,0.32,0.3,0.31,0.4,0.2,0.25,0.22,0.29,0.38,0.25,0.34,0.34,0.31,0.3,0.21,0.16,0.18,0.18,0.18,0.11,0.28,0.31,0.18,0.27,0.25,0.23,0.24,0.11,0.24,0.15,0.21,0.16,0.26,0.13,0.22,0.28,0.28,0.16,0.12,0.2,0.26,0.26,0.23,0.25,0.19,0.25,0.21,0.23,0.19,0.15,0.16,0.25,0.2,0.32,0.22,0.15,0.21,0.14,0.16,0.26,0.24,0.28,0.3,0.31,0.35,0.14,0.24,0.21,0.25,0.23,0.11,0.15,0.3,0.28,0.25,0.23,0.23,0.25,0.29,0.27,0.14,0.32,0.22,0.19,0.16,0.23,0.2,0.19,0.22,0.19,0.18,0.22,0.24,0.12,0.16,0.27,0.3,0.21,0.13,0.21,0.21,0.36,0.31,0.31,0.13,0.16,0.19,0.33,0.12,0.17,0.11,0.18,0.28,0.32,0.18,0.22,0.26,0.29,0.19,0.17,0.24,0.26,0.24,0.21,0.19,0.26,0.21,0.21,0.35,0.27,0.29,0.21,0.28,0.12,0.23,0.15,0.18,0.13,0.17,0.28,0.23,0.35,0.2,0.26,0.14,0.17,0.27,0.18,0.16,0.2,0.18,0.26,0.32,0.29,0.12,0.24,0.21,0.2,0.22,0.21,0.24,0.31,0.14,0.15,0.19,0.24,0.28,0.29,0.24,0.3,0.24,0.21,0.3,0.15,0.29,0.37,0.2,0.24,0.29,0.19,0.2,0.21,0.35,0.28,0.21,0.13,0.23,0.19,0.42,0.23,0.2,0.26,0.26,0.12,0.35,0.3,0.19,0.33,0.39,0.27,0.27,0.23,0.18,0.24,0.32,0.25,0.38,0.29,0.16,0.28,0.28,0.22,0.25,0.2,0.25,0.24,0.28,0.24,0.18,0.25,0.19,0.16,0.26,0.24,0.34,0.18,0.22,0.16,0.21,0.27,0.33,0.19,0.33,0.34,0.34,0.46,0.21,0.2,0.21,0.16,0.12,0.27,0.33,0.21,0.19,0.33,0.34,0.19,0.14,0.14,0.22,0.14,0.21,0.3,0.26,0.16,0.18,0.13,0.37,0.28,0.23,0.24,0.16,0.21,0.27,0.25,0.21,0.22,0.23,0.25,0.23,0.23,0.21,0.18,0.14,0.22,0.2,0.21,0.12,0.24,0.2,0.2,0.32,0.32,0.21,0.23,0.21,0.19,0.19,0.17,0.33,0.15,0.17,0.16,0.15,0.13,0.21,0.2,0.22,0.15,0.19,0.18,0.29,0.22,0.24,0.19,0.19,0.2,0.26,0.24,0.19,0.18,0.26,0.15,0.27,0.26,0.25,0.25,0.24,0.29,0.11,0.21,0.25,0.23,0.25,0.36,0.21,0.23,0.2,0.18,0.17,0.13,0.23,0.28,0.24,0.21,0.27,0.18,0.14,0.19,0.18,0.16,0.18,0.089,0.28,0.25,0.22,0.21,0.18,0.21,0.17,0.26,0.32,0.27,0.26,0.27,0.2,0.21,0.095,0.17,0.21,0.12,0.11,0.1,0.08,0.12,0.26,0.24,0.2,0.26,0.2,0.17,0.19,0.13,0.16,0.29,0.29,0.23,0.35,0.26,0.21,0.2,0.21,0.22,0.23,0.24,0.24,0.2,0.2,0.15,0.12,0.11,0.24,0.25,0.19,0.22,0.23,0.18,0.26,0.19,0.24,0.17,0.14,0.3,0.22,0.24,0.098,0.25,0.22,0.25,0.15,0.26,0.21,0.27,0.21,0.21,0.17,0.24,0.22,0.2,0.24,0.17,0.23,0.14,0.15,0.21,0.14,0.24,0.19,0.21,0.2,0.15,0.23,0.14,0.21,0.24,0.21,0.26,0.24,0.29,0.24,0.22,0.15,0.25,0.22,0.12,0.19,0.23,0.18,0.14,0.24,0.13,0.26,0.15,0.13,0.29,0.25,0.25,0.13,0.2,0.12,0.15,0.12,0.21,0.19,0.19,0.14,0.17,0.095,0.32,0.23,0.2,0.16,0.22,0.098,0.12,0.23,0.21,0.19,0.26,0.18,0.11,0.2,0.14,0.36,0.17,0.2,0.22,0.15,0.21,0.15,0.28,0.14,0.17,0.21,0.19,0.34,0.18,0.15,0.29,0.22,0.11,0.15,0.19,0.24,0.27,0.26,0.25,0.21,0.2,0.23,0.21,0.18,0.24,0.15,0.17,0.18,0.19,0.11,0.24,0.25,0.26,0.2,0.12,0.13,0.16,0.2,0.27,0.22,0.35,0.18,0.2,0.24,0.22,0.13,0.41,0.2,0.17,0.23,0.2,0.27,0.16,0.17,0.23,0.22,0.27,0.22,0.24,0.27,0.24,0.17,0.27,0.2,0.27,0.14,0.23,0.22,0.15,0.21,0.25,0.18,0.25,0.21,0.19,0.12,0.25,0.2,0.19,0.3,0.33,0.14,0.094,0.27,0.13,0.23,0.24,0.25,0.29,0.28,0.22,0.31,0.12,0.25,0.19,0.22,0.25,0.31,0.31,0.17,0.24,0.33,0.27,0.15,0.15,0.25,0.3,0.3,0.21,0.24,0.39,0.21,0.22,0.22,0.25,0.11,0.26,0.19,0.21,0.28,0.096,0.11,0.27,0.17,0.15,0.19,0.23,0.2,0.34,0.21,0.16,0.32,0.2,0.28,0.34,0.28,0.32,0.31,0.25,0.14,0.22,0.16,0.14,0.2,0.15,0.24,0.21,0.19,0.35,0.33,0.21,0.23,0.4,0.21,0.26,0.22,0.2,0.22,0.21,0.19,0.23,0.26,0.25,0.21,0.33,0.29,0.16,0.24,0.22,0.16,0.18,0.17,0.14,0.19,0.13,0.18,0.21,0.27,0.15,0.15,0.23,0.28,0.19,0.19,0.16,0.33,0.16,0.15,0.18,0.15,0.26,0.25,0.15,0.2,0.26,0.21,0.16,0.19,0.22,0.32,0.26,0.12,0.26,0.19,0.17,0.23,0.2,0.18,0.16,0.15,0.22,0.3,0.21,0.17,0.15,0.23,0.23,0.19,0.22,0.23,0.14,0.25,0.24,0.23,0.18,0.25,0.27,0.19,0.28,0.16,0.2,0.21,0.22,0.2,0.2,0.19,0.2,0.19,0.14,0.19,0.094,0.15,0.17,0.12,0.23,0.23,0.14],
  \"duration\":881.164734
}
")

(def json-test-data
  (.parse js/JSON json-test-string))

(def test-data
  (js->clj json-test-data :keywordize-keys true))

(defn waveform
  "returns waveform reagent component given noise level input"
  ([amplitude-data]
  (let [amplitude (js->clj amplitude-data)]
    (waveform-svg amplitude 300 200)))
  ([]
   (waveform-svg test-data 1200 300)))



  
