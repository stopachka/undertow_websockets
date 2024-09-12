(ns stopachka.alpha-cel
  (:require [stopachka.cel :as cel]
            [stopachka.async :as a])
  (:gen-class))

(defn -main
  [& _]
  (let [code "data.creatorEmail == auth.email"
        ast (cel/->ast code)
        program (cel/->program ast)]
    (println
     (count (a/vfuture-pmap (fn [_]
                              (cel/eval-program!
                               {:cel-program program}
                               {"auth" (cel/->cel-map {"email" "stepan.p@gmail.com"})
                                "data" (cel/->cel-map {"id" #uuid "8164fb78-6fa3-4aab-8b92-80e706bae93a"
                                                       "creatorEmail" "stepan.p@gmail.com"
                                                       "name" "Nonfiction"
                                                       "_etype" "bookshelves"})}))

                            (range 1000))))))


