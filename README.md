# Core async intro #

Rich Hickeys [blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html)



## Channels ##

-----------------------------

(defn make-rand-chan []
  (let [c (chan)]
    (go (while true
          (>! c (rand))))
    c))
-----------------------------

## Go blocks ##


## Go "patterns" ##

## Filter ##


## Agregate ##


## Fan in/out ##


## Quit and timeout##


## Links ##

