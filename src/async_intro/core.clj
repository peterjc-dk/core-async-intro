(ns async-intro.core
  (:require [clojure.core.async :refer :all]))

(let [ch (chan)]
  (thread (>!! ch "hello"))
  (println (<!! ch)))


(let [ch (chan)]
  (go (>! ch "hello go"))
  (go (println (<! ch))))

(println (<!! (go "go chan")))

(defn make-rand-chan []
  (let [c (chan)]
    (go (while true
          (>! c (rand))))
    c))

(defn accum-chan [in-c]
  (let [c (chan)]
    (go (loop [n 0 acc 0]
          (let [in-val (<! in-c)
                n-next (inc n)
                acc-next (+ acc in-val)]
            (>! c [in-val n-next acc-next])
            (recur n-next acc-next))))
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
            (condp = ch
              in-chan
              (do
                (println (str "sniff: " v))
                (>! c v)
                (recur))
              quit-chan
              (>! v "sniff stopped")))))
    c))

(defn sink-chan
  "consume and a chan and listen to quit chan"
  [in-chan quit-chan]
  (go (loop []
        (let [[v ch] (alts! [quit-chan in-chan])]
          (condp = ch
            in-chan
            (do
              (println (str "sink: " v))
              (Thread/sleep 100)
              (recur))
            quit-chan
            (>! v "sink stopped"))))))

(defn go-stop [qc]
  (go (let [qq (chan)]
        (>! qc qq)
        (<! qq))))

(defn do-some-random-stuff []
  (let [q1 (chan)
        q2 (chan)
        input-chan (make-rand-chan)
        sniffed-chan (sniff-chan input-chan q1)
        result-chan (above-avarage sniffed-chan)]
    (sink-chan result-chan q2)
    (go (<! (timeout 1000))
        (println "stopping..")
        (println (<! (go-stop q1)))
        (println (<! (go-stop q2)))
        (println "Done!"))))

(do-some-random-stuff)
