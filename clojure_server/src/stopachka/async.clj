
(ns stopachka.async
  (:refer-clojure :exclude [future-call])
  (:import [java.util.concurrent Executors ExecutorService]))

(def ^ExecutorService default-virtual-thread-executor (Executors/newVirtualThreadPerTaskExecutor))

(defn ^:private deref-future
  "Private function copied from clojure.core;

  A helper that derefs a future in the shape that IBlockingDeref needs."
  ([^java.util.concurrent.Future fut]
   (.get fut))
  ([^java.util.concurrent.Future fut timeout-ms timeout-val]
   (try (.get fut timeout-ms java.util.concurrent.TimeUnit/MILLISECONDS)
        (catch java.util.concurrent.TimeoutException _
          timeout-val))))

(defn future-call
  "Like clojure.core/future-call, but accepts an Executor"
  [^ExecutorService executor f]
  (let [f (bound-fn* f)
        fut (.submit executor ^Callable f)]
    (reify
      clojure.lang.IDeref
      (deref [_] (deref-future fut))
      clojure.lang.IBlockingDeref
      (deref
        [_ timeout-ms timeout-val]
        (deref-future fut timeout-ms timeout-val))
      clojure.lang.IPending
      (isRealized [_] (.isDone fut))
      java.util.concurrent.Future
      (get [_] (.get fut))
      (get [_ timeout unit] (.get fut timeout unit))
      (isCancelled [_] (.isCancelled fut))
      (isDone [_] (.isDone fut))
      (cancel [_ interrupt?] (.cancel fut interrupt?)))))

(defmacro vfuture
  "Takes a body of expressions and yields a future object that will
  invoke the body in a **virtual thread**, and will cache the result and
  return it on all subsequent calls to deref/@. If the computation has
  not yet finished, calls to deref/@ will block, unless the variant of
  deref with timeout is used. See also - realized?."
  [& body]
  `(future-call default-virtual-thread-executor (^{:once true} fn* [] ~@body)))

(defn vfuture-pmap
  "Like pmap, but uses vfutures to parallelize the work.

  Why would you want to use this instead of pmap?

  pmap has a fixed size threadpool. This means that if you have a recursive
  function that uses `pmap`, you can run into a deadlock.

  vfutures on the other hand user a virtual-thread-per-task executor. 
  This executor does not have any bounds, so even if you have a recursive function, 
  you won't deadlock."
  [f coll]
  (let [futs (mapv #(vfuture (f %)) coll)]
    (mapv deref futs)))

(comment
  (vfuture 1)
  (vfuture-pmap inc (range 10)))
