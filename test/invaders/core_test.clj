(ns invaders.core-test
  (:require [clojure.test :refer [is deftest testing]]
            [invaders.core :refer [str->grid] :as sut]))

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

(deftest line-radar-test
  (let [empty-radar-1x3 (str->grid "---")
        middle-radar-1x3 (str->grid "-o-")
        left-radar-1x3 (str->grid "o--")
        right-radar-1x3 (str->grid "--o")
        left-invader (str->grid "-o")
        right-invader (str->grid "o-")]
    (let [invader (str->grid "oo")]
      (is (empty? (sut/find-pattern empty-radar-1x3 invader))))

    ;(sut/new-print-grid-with-patterns left-radar-1x3 left-invader)

    (testing "Left invaders"
      (is (= [[0 2]] (sut/find-pattern empty-radar-1x3 left-invader))
          "Potential invader on the right side")
      (is (= [[0 -1] [0 2]] (sut/find-pattern left-radar-1x3 left-invader))
          "Potential invader on the right side")
      (is (= [[0 0] [0 2]] (sut/find-pattern middle-radar-1x3 left-invader))
          "Potential invader on the left side and exact invader")
      (is (= [[0 1]] (sut/find-pattern right-radar-1x3 left-invader))
          "Exact invader"))

    (testing "Right invaders"
      (is (= [[0 -1]] (sut/find-pattern empty-radar-1x3 right-invader))
          "Potential invader on the left side")
      (is (= [[0 0]] (sut/find-pattern left-radar-1x3 right-invader))
          "Potential invader on the right side")
      (is (= [[0 -1] [0 1]] (sut/find-pattern middle-radar-1x3 right-invader))
          " Potential invader on the left side and exact invader ")
      (is (= [[0 -1] [0 2]] (sut/find-pattern right-radar-1x3 right-invader))
          "Exact invader "))))

(deftest radar-3x4-test
  (let [radar (str->grid "o--
                          -oo
                          o--
                          --o")]
    (sut/print-radar-with-invaders radar (str->grid "-o
                                                        o-"))
    (is (= [[-1 0]
            [0 -1]
            [0 2]
            [1 0]
            [2 -1]
            [2 2]
            [3 1]]
           (sut/find-pattern radar (str->grid "-o
                                               o-"))))
    (is (= [[-1 -1]
            [3 2]]
           (sut/find-pattern radar (str->grid "oo
                                               oo"))))
    (is (= [[-1 -1]
            [-1 2]
            [0 0]
            [1 -1]
            [1 2]
            [3 -1]
            [3 2]]
           (sut/find-pattern radar (str->grid "o-
                                               -o"))))))

(defn- file->grid [file-path]
  (str->grid (slurp file-path)))

(deftest file->grid-test
  (is (= [[\- \o]
          [\o \-]]
         (file->grid "resources/invaders/3.txt"))))

(deftest readme-test
  (let [radar (str->grid (slurp "resources/radar/1.txt"))
        invader1 (str->grid (slurp "resources/invaders/1.txt"))
        invader2 (str->grid (slurp "resources/invaders/2.txt"))]
    ;(sut/print-radar-with-invaders)
    (sut/print-radar-with-invaders radar (str->grid (slurp "resources/invaders/2.txt")))
    ;; TODO check the actual pattern
    (is (seq (sut/find-pattern radar invader1)))
    (is (seq (sut/find-pattern radar invader2)))))
