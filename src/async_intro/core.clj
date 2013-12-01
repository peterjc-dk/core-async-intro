(ns async-intro.core
  (:require [clojure.core.async :refer :all]))


(defn make-rand-chan []
  (let [c (chan)]
    (go (while true
          (>! c (rand))))
    c))

(defn bigger-than? [limit]
  (fn [x] (< limit x)))

(defn accum-chan [in-c]
  (let [c (chan)]
    (go (loop [n 0 acc 0]
          (let [in-val (<! in-c)
                n-next (inc n)
                acc-next (+ acc in-val)]
            (>! c [in-val n-next acc-next])
            (recur n-next acc-next))))
    c))

(defn bigger-than-chan [in-c limit]
  (let [c (chan)
        big-pred-lim (bigger-than? limit)]
    (go (while true
          (let [val (<! in-c)]
            (>! c [val (big-pred-lim val)]))))
    c))

(defn above-avarage [in-c]
  (let [c (chan)
        acc-c (accum-chan in-c)]
    (go (while true
          (let [[in-val n acc] (<! acc-c)
                mean (/ acc n)
                above (< mean in-val)]
            (>! c {:val in-val
                   :mean mean
                   :over above}))))
    c))

(defn split-c [in-c]
  (let [c1 (chan)
        c2 (chan)]
    (go (while true
          (let [in-val (<! in-c)]
            (go (>! c1 in-val))
            (go (>! c2 in-val)))))
    [c1 c2]))

(defn merge-c [in-c1 in-c2]
  (let [c (chan)]
    (go (while true
          (let [[v ch] (alts! [in-c1 in-c2])]
            (>! c v))))))

(defn fn-c [in-c f]
  (let [c (chan)]
    (go (while true
          (>! c (f (<! in-c)))))
    c))


(defn sniff-chan
  "sniff and forward a chan and listen to quit chan"
  [in-chan quit-chan]
  (let [c (chan)]
    (go (loop []
          (let [[v ch] (alts! [quit-chan in-chan])]
            (cond (= ch in-chan)
                  (do
                    (println (str "sniff: " v))
                    (>! c v)
                    (recur))
                  (= ch quit-chan)
                  (do (println "stopped")
                      (close! c))))))
    c))

(defn sink-chan
  "consume and a chan and listen to quit chan"
  [in-chan quit-chan]
  (let [c (chan)]
    (go (loop []
          (let [[v ch] (alts! [quit-chan in-chan])]
            (cond (= ch in-chan)
                  (do
                    (println (str "sink: " v))
                    (recur))
                  (= ch quit-chan)
                  (do (println "sink stopped")
                      (close! c))))))
    c))
