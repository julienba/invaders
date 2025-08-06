(ns invaders.core-test
  (:require [clojure.test :refer [is deftest]]
            [invaders.core :as sut]))

(deftest str->grid-test
  (let [invader-4x3 "--o-
                     ---o
                     --oo"]
    (is (= [[\- \- \o \-]
            [\- \- \- \o]
            [\- \- \o \o]]
           (sut/str->grid invader-4x3)))))

(deftest simple-radar-test
  (let [simple-radar (sut/str->grid "---
                                     o--
                                     ---")
        invader (sut/str->grid "o")
        reverse-invader (sut/str->grid "-")]
    (is (= [[1 0]]
           (sut/find-pattern simple-radar invader)))
    (is (= [[0 0] [0 1] [0 2] [1 1] [1 2] [2 0] [2 1] [2 2]]
           (sut/find-pattern simple-radar reverse-invader)))))
