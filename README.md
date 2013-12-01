# Core async intro #

This repo contains a short introduction to clojure's core.async libery, as presented at a Clojure user group meeting in Copenhagen.

The introduction of core.async is relative new in cojure though not a new concept in general, and in
 [this blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) Rich Hickey motivas and explains it.

This introduction will only glanse the different concepts since other good introductions already exist. See below for a list of links.

Besides the material published by Hickey and Nolan, I will higly recommend studing the Go language's concurrency concepts, and of course the source of these ideas, like the articles of C. A. R. Hoare.


## Channels ##
Creating and using a channel, by default it is blocking, non buffered.

    (let [ch (chan)]
      (thread (>!! ch "hello"))
      (println (<!! ch)))

This code snippet, creats a chan, puts "hello" in the chan, and block the thread waiting for someone to consume the "hello". the main thread takes the "hello" (will block if the chan is empty), and prints it out.

This way chan's are used to communicate between threads, but also as a syncronization.


## Go blocks ##
Creating a thread for each sub-task does not scale, to solve this go routine can be used, these are light weigt threads, cheap to create and posibble to have 100 or 1000 at the same time.


    (let [ch (chan)]
      (go (>! ch "hello"))
      (go (<! ch)))

The go block it self returns a chan containing the result of the encapsulated expresion.

    (println (<!! (go "go chan")))


## Go "patterns" ##

    (defn make-rand-chan []
      (let [c (chan)]
        (go (while true
              (>! c (rand))))
        c))





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
