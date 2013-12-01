# Core async intro #

This repo contains a short introduction to clojure's core.async libery, as presented at a Clojure user group meeting in Copenhagen.

The introduction of core.async is relative new in cojure though not a new concept in general, and in
 [this blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) Rich Hickey motivas and explains it.

This introduction will only clanse the different concepts since other good introductions already exist. See below for a list of links.

Besides the material published by Hickey and Nolan, I will higly recommend studing the Go language's concurrency concepts, and of course the source of these ideas, like the articles of C. A. R. Hoare.


## Channels ##


    (defn make-rand-chan []
      (let [c (chan)]
        (go (while true
              (>! c (rand))))
        c))


## Go blocks ##


    (defn make-rand-chan []
      (let [c (chan)]
        (go (while true
              (>! c (rand))))
        c))


## Go "patterns" ##


## Filter ##


## Agregate ##


## Fan in/out ##


## Quit and timeout##


## Links to articles and talks##

* [Hickey blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html)

* [Hickey Strangeloop talk](http://www.infoq.com/presentations/clojure-core-async)

* [walkthrough](https://github.com/clojure/core.async/blob/master/examples/walkthrough.clj)

* [Nolan blogposts](http://swannodette.github.io/)

* [Nolan talk/demo](http://www.youtube.com/watch?v=AhxcGGeh5ho)

* [Go talk 'Go concurrency patterns'](http://www.youtube.com/watch?v=f6kdp27TYZs)

* [Go talk 'Concurency is not parallelism'](http://www.youtube.com/watch?v=cN_DpYBzKso)

* [C. A. R. Hoare. Communicating Sequential Processes. (1978)](http://www.cs.cmu.edu/~crary/819-f09/Hoare78.pdf)

* [Go talk 'Let's Go Further: Build Concurrent Software using the Go Programming Language'](http://www.youtube.com/watch?v=cN_DpYBzKso)
