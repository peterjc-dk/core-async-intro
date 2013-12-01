# Core async intro #

This repo contains a short introduction to clojure's core.async libery, as presented at a Clojure user group meeting in Copenhagen.

The introduction of core.async is relative new, and in
 [this blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) Rich Hickey motivas and explains it.

This introduction will only clanse the different concepts since other good introductions already exist. See below for a list of links.

## Channels ##


    (defn make-rand-chan []
      (let [c (chan)]
        (go (while true
              (>! c (rand))))
        c))


## Go blocks ##


## Go "patterns" ##

## Filter ##


## Agregate ##


## Fan in/out ##


## Quit and timeout##


## Links ##
