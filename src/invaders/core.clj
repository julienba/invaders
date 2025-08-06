(ns invaders.core
  (:require [clojure.string :as string]))

;; TODO check that the string is a grid: size and chars used
(defn str->grid
  "Convert a string into a 2D array
   NOTE: It would be more memory efficient with make-array"
  [s]
  (into [] (for [line (string/split-lines s)]
             (into [] (remove #{\space} (seq line))))))

(defn pattern-matches?
  [grid pattern start-row start-col]
  (let [grid-rows (count grid)
        grid-cols (count (first grid))
        pattern-rows (count pattern)
        pattern-cols (count (first pattern))]
    ;; Check each cell in the pattern that overlaps with the grid
    (every? true?
            (for [r (range pattern-rows)
                  c (range pattern-cols)
                  :let [grid-row (+ start-row r)
                        grid-col (+ start-col c)]
                  :when (and (>= grid-row 0)
                             (>= grid-col 0)
                             (< grid-row grid-rows)
                             (< grid-col grid-cols))]
              (= (get-in grid [grid-row grid-col])
                 (get-in pattern [r c]))))))

(defn find-pattern
  "Returns a sequence of [row col] coordinates where the pattern starts."
  [grid pattern]
  (let [grid-rows (count grid)
        grid-cols (count (first grid))]
    ;; Allow patterns to start at any position in the grid
    (for [row (range grid-rows)
          col (range grid-cols)
          :when (pattern-matches? grid pattern row col)]
      [row col])))