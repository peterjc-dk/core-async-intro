(ns async-intro.search
  (:require [clojure.core.async :refer :all]))

;; this is me trying to follow along in one of Rob pikes go talks.
;; the examples in core.async also implements the same example.

(defn fake-search
  "create fake search fn"
  [kind]
  (fn [query]
    (do
      (Thread/sleep (rand-int 1000))
      (println "start " kind)
      (str  kind ":" query))))

(defn google-v1
  "make the search"
  [query]
  (let [web-search (fake-search "Web")
        image-search (fake-search "Image")
        video-search (fake-search "Video")]
    [(web-search query)
     (image-search query)
     (video-search query)]))

(defn google-v2
  "make the search"
  [query]
  (let [web-search (fake-search "Web")
        image-search (fake-search "Image")
        video-search (fake-search "Video")
        result-chan (chan)]
    (go (>! result-chan (web-search query)))
    (go (>! result-chan (image-search query)))
    (go (>! result-chan (video-search query)))
    [(<!! result-chan) (<!! result-chan) (<!! result-chan)]))

(defn google-v2-1
  "make the search"
  [query]
  (let [web-search (fake-search "Web")
        image-search (fake-search "Image")
        video-search (fake-search "Video")
        result-chan (chan)
        t (timeout 50)]
    (go (>! result-chan (web-search query)))
    (go (>! result-chan (image-search query)))
    (go (>! result-chan (video-search query)))
    (map first [(alts!! [result-chan t])
      (alts!! [result-chan t])
      (alts!! [result-chan t])])))

(defn first-chan
  "get result from the first ready chan"
  [& cs]
  (let [result-chan (chan)]
    (loop [css cs]
      (if (nil? css)
        (do
          (println "over?")
          (alts!! [result-chan (timeout 1000)] ))
        (do
         (println "c:" (first css))
         (go (let [c (first css)
                   r (<! c)]
               (println "r:" r)
               (>! result-chan r)))
          (recur (next css)))))))

(defn first-to-chan
  "get result and put in chan"
  [& cs]
  (let [result-chan (chan)]
    (loop [css cs]
      (if (nil? css)
        (first (alts!! [result-chan (timeout 1000)]))
        (do
         (println "pop")
         (go (>! result-chan (first css)))
         (recur (next css)))))))

(defn google-v3
  "make the search"
  [query]
  (let [web-search-1 (fake-search "Web1")
        web-search-2 (fake-search "Web2")
        image-search-1 (fake-search "Image1")
        image-search-2 (fake-search "Image2")
        video-search-1 (fake-search "Video1")
        video-search-2 (fake-search "Video2")
        result-chan (chan)
        t (timeout 5000)]
    (go (>! result-chan (first-to-chan
                         (web-search-1 query)
                         (web-search-2 query))))
    (go (>! result-chan (first-to-chan
                         (image-search-1 query)
                         (image-search-2 query))))
    (go (>! result-chan (first-to-chan
                         (video-search-1 query)
                         (video-search-2 query))))
    (map first [(alts!! [result-chan t])
      (alts!! [result-chan t])
      (alts!! [result-chan t])])))
