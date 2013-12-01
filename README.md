# Core async intro #

This repo contains a short introduction to clojure's core.async library, as presented at a Clojure user group meeting in Copenhagen.

The introduction of core.async is relative new in Clojure though not a new concept in general, and in
 [this blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) Rich Hickey motivates and explains it.

This introduction will only glance the different concepts since other good introductions already exist. See below for a list of links.

Besides the material published by Hickey and Nolan, I will higly recommend studying the Go language's concurrency concepts, and of course the source of these ideas, like the articles of C. A. R. Hoare. Links to what I found interesing is also in the list of links.


## Channels ##
Creating and using a channel, by default it is blocking, non buffered.

    (let [ch (chan)]
      (thread (>!! ch "hello"))
      (println (<!! ch)))

This code snippet, creates a chan, puts "hello" in the chan, and block the thread waiting for someone to consume the "hello". the main thread takes the "hello" (will block if the chan is empty), and prints it out.

This way chan's are used to communicate between threads, but also as a synchronisation.


## Go blocks ##
Creating a thread for each sub-task does not scale, to solve this go routine can be used, these are light weight threads, cheap to create and it is possible to have 100 or 1000 at the same time. The mapping to real threads/thread pool is handle under the hood.


    (let [ch (chan)]
      (go (>! ch "hello"))
      (go (<! ch)))

The go block it self returns a chan containing the result of the encapsulated expression.

    (println (<!! (go "go chan")))

Note that inside a go routine we use one "!" outside two "!!".

## Go "patterns" ##
When Writing async code some typical ways to structure your code emerges.

    (defn make-rand-chan []
      (let [c (chan)]
        (go (while true
              (>! c (rand))))
        c))

This little snippet shows two simple concepts, first a go with a loop inside, waiting for the result to be consumed before continuing the loop. Second all this is wrapped in a function that constructs/setup the go routine, and returning a output chan.

Here a small function constructing a go routine that applies a function and pass it on.

    (defn fn-c [in-c f]
      (let [c (chan)]
        (go (while true
              (>! c (f (<! in-c)))))
        c))

## Fan in/out ##
Small function that reads from one chan and write to two.

    (defn split-c [in-c]
      (let [c1 (chan)
            c2 (chan)]
        (go (while true
              (let [in-val (<! in-c)]
                (go (>! c1 in-val))
                (go (>! c2 in-val)))))
        [c1 c2]))

Another function that reads from two chan's and writes to one.

    (defn merge-c [in-c1 in-c2]
      (let [c (chan)]
        (go (while true
              (let [[v ch] (alts! [in-c1 in-c2])]
                (>! c v))))))

the Alts let you listen/wait for a result from multiple chan's. By default it will take one at random if more values are available, but it is possible to have them priorities instead.


## Aggregate ##
Here a bit more involved example that accumulate the value of what is read from the in-chan. This illustrate another interesting pattern, namely now our small go routine wraps state, by using a loop instead of just a while.

    (defn accum-chan [in-c]
      (let [c (chan)]
        (go (loop [n 0 acc 0]
              (let [in-val (<! in-c)
                    n-next (inc n)
                    acc-next (+ acc in-val)]
                (>! c [in-val n-next acc-next])
                (recur n-next acc-next))))
        c))

Note that this is a toy example, and implicit assumes that the in-val is a number.

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
Here above a go routine that uses the accumulator routine.



## Quitting ##
When putting real things together it is use full to be able to tell a go routine to quit, the "recommended" way to do this in the go language is to have quit chan's. This small channel sniffer shows this done in Clojure.


    (defn sniff-chan
      "sniff and forward a chan and listen to quit chan"
      [in-chan quit-chan]
      (let [c (chan)]
        (go (loop []
              (let [[v ch] (alts! [quit-chan in-chan] :priority true)]
                (condp = ch
                  in-chan
                  (do
                    (println (str "sniff: " v))
                    (>! c v)
                    (recur))
                  quit-chan
                  (>! v "sniff stopped")))))
        c))

Also taken from go-lang is the notion that the quitting is done by passing a new channel through the quit-chan, for signalling when shut-down is done. (so yes channels can be put through channels)

Such a go routine is also use full, test/debug tool when creating real async code.

Also use full when integrating async code is a routine that can just consume what is put in a chan.

    (defn sink-chan
      "consume from a chan and listen to quit chan"
      [in-chan quit-chan]
      (go (loop []
            (let [[v ch] (alts! [quit-chan in-chan] :priority true)]
              (condp = ch
                in-chan
                (do
                  (println (str "sink: " v))
                  (Thread/sleep 100)
                  (recur))
                quit-chan
                (>! v "sink stopped"))))))

## Putting things together ##
Finally a example on how things can be wired together.

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

## Final thoughs ##

On one hand it is nice and simple to plug channels together like this in a let block, but I find this quickly grows in complexity as our application does. Especially if you as I have here also use quit channels.

Im sure it will be possible to cook up a generic way of putting go routines together, and perhaps wrap some error handling, I just haven’t cracked it yet.

This final example also illustrates a important feature, namely timeout channels. The typical way of using these is in a alts! where you then either wait for a channel to have a value for you or the timeout channel to deliver ensuring you don't block reading from a channel forever.

The concept of separating parts of a system and putting channels between them is a very power full concept. It makes it natural to create concurrent systems and make the communication data oriented. Especially since what is passed around is (of course) is immutable.

## Example code ##
All the snippets are in this repository "src/async_intro/core.clj"

Also included is me folling along and implemeting the examples from Rob Pikes [Go talk 'Go concurrency patterns'](http://www.youtube.com/watch?v=f6kdp27TYZs). This is a very good way to wrap your head around the concept, the different mindset and play with clojure async syntax. Properly not high quality async code since it was me trying to learn :)


## Links to articles and talks ##

* [Hickey blogpost](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html)
* [Hickey Strangeloop talk](http://www.infoq.com/presentations/clojure-core-async)
* [walkthrough](https://github.com/clojure/core.async/blob/master/examples/walkthrough.clj)
* [Nolan blogposts](http://swannodette.github.io/) (A lot of real cool stuff, ClojureScript combined with core.async is a killer feature!)
* [Nolan talk/demo](http://www.youtube.com/watch?v=AhxcGGeh5ho)
* [Go talk 'Go concurrency patterns'](http://www.youtube.com/watch?v=f6kdp27TYZs)
* [Go talk 'Concurency is not parallelism'](http://www.youtube.com/watch?v=cN_DpYBzKso)
* [C. A. R. Hoare. Communicating Sequential Processes. (1978)](http://www.cs.cmu.edu/~crary/819-f09/Hoare78.pdf)
* [Go talk 'Let's Go Further: Build Concurrent Software using the Go Programming Language'](http://www.youtube.com/watch?v=4iAiS-qv26Q)

## License ##

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.
