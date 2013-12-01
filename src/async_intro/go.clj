(ns async-intro.go
  (:require [clojure.core.async :refer :all]))
;; this is me trying to follow along in one of Rob pikes go talks.

(defn boring
  "a boring function"
  [c msg]
  (go (loop [i 0]
        (if (> i 40)
          msg
          (do (>! c (str msg " " i))
              (Thread/sleep (rand-int 1000))
              (recur (inc i)))))))

(defn boring-wait
  "boring fn use a wait chan"
  [msg]
  (let [c (chan)
        wait (chan)]
    (go (dotimes [n 10]
          (>! c {:msg (str msg "-" n) :wait wait} )
          (Thread/sleep (rand-int 1000))
          (<! wait)))
    c))


(defn make-boring-chan
  "makes a channel using boring fn"
  [msg]
  (let [c (chan)]
    (boring c msg)
    c))

(defn boring-quit
  "a boring chan with a quit chan"
  [msg quit-chan]
  (let [c (chan)]
    (go (loop [quit false]
          (if quit
            (do (println "bye!")
                (go (>! quit-chan "byebye"))
                "bye")
            ;; else
            (do
               (println "hello")
               (>! c msg)
               (let [[v ch] (alts! [quit-chan] :default false)]
                 (recur v))))))
    c))

(defn boring-quit-2
  "a boring chan with a quit chan"
  [msg quit-chan]
  (let [c (chan)]
    (go (loop []
          (do
            (println "hello 3")
            (let [t (timeout 10000)
                  cg (go (>! c msg))
                  [v ch] (alts! [quit-chan cg t])]
              (cond (= ch cg)
                    (do
                      (println "ok")
                      (recur))
                    (= ch quit-chan)
                    (do (println "stopped")
                        (close! c))
                    (= ch t)
                    (println "timeout"))))))
    c))

(defn joe-ann
  "the channel as a handle example"
  []
  (let [joe (make-boring-chan "joe")
        ann (make-boring-chan "ann")]
    (dotimes [n 5]
      (println (str "f:"(<!! joe)))
      (println (str "f:"(<!! ann))))))

(defn fan-in-simple [c1 c2]
  (let [c (chan)]
    (go (while true (>! c (<! c1))))
    (go (while true (>! c (<! c2))))
    c))

(defn fan-in
  "alt fan in"
  [b4 c1 c2]
  (let [c (chan)]
    (go (while true
          (let [[v ch] (alts! [b4 c1 c2] :priority true)]
            (cond (= ch b4) (>! c [v :b4])
                  (= ch c1) (>! c [v :c1])
                  (= ch c2) (>! c [v :c2])))))
    c))

(defn joe-ann-fan-in
  "the channel as a handle example"
  [b4]
  (let [c (fan-in
           b4
           (make-boring-chan "joe")
           (make-boring-chan "ann"))]
    (dotimes [n 200]
      (println (str "f:" (<!! c))))))

(defn joe-ann-fan-in-wait
  "the channel as a handle example"
  []
  (let [c (fan-in-simple
           (boring-wait "joe")
           (boring-wait "ann"))]
    (dotimes [n 10]
      (let [m (<!! c)]
        (println (str "f:" (:msg m)))
        (>!! (:wait m) true)))))

(defn joe-ann-fan-in-with-quit
  "the channel as a handle example"
  [quit]
  (let [c (fan-in-simple
           (boring-quit "joe" quit)
           (boring-quit "ann" quit))]
    (dotimes [n 10]
      (let [m (<!! c)]
        (println (str "msg:" m))))
    (>! quit true)
    (println (str "let say: " (<! quit)))))

(defn chain-fn
  "chain two channels together"
  [left-c right-c]
  (go (>! left-c (inc (<! right-c)))))

(defn make-chain
  "chain some channels together"
  []
  (let [leftmost (chan)
        rightmost (chan)]
    (loop [left-c  leftmost right-c (chan) i 1]
      (if (< i 100000)
        (do
          (chain-fn left-c right-c)
          (recur right-c (chan) (inc i)))
        ;; else
        (chain-fn left-c rightmost)))
    (go (>! rightmost 0))
    (println (str "chain " (<!! leftmost)))))

;; some fun with lazy seq and channels
;; later found this use full for testing

(defn chan-2-lazy-seq
  "chan to lazy seq"
  [in-chan]
  (fn lz [] (cons (<!! in-chan) (lazy-seq (lz)))))

(defn ch-2-lazy
  "chan to lazy seq"
  [in-chan]
  (cons (<!! in-chan) (lazy-seq (ch-2-lazy in-chan))))

(defn make-rand-chan []
  (let [c (chan)]
    (go (while true
          (>! c (rand))))
    c))

(defn sq-2-chan [sq]
  (let [c (chan)]
    (go (loop [s sq]
          (if s
            (do (>! c (first s))
                (recur (rest s))) "Stop")))
    c))

(comment
  (def c (make-rand-chan))
  (def lz (chan-2-lazy-seq c))

  (take 2 (lz))
  (reduce + (take 10 (lz)))
  (reduce + (take 100 (ch-2-lazy c)))

  (def s [1 2 23 3 4 4 4])

  (def c1 (sq-2-chan s))

  (<!! c1))
