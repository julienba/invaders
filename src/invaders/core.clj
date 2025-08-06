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
        grid-cols (count (first grid))
        pattern-rows (count pattern)
        pattern-cols (count (first pattern))
        ;; Allow patterns to start before and beyond the grid boundaries
        ;; but ensure at least one character overlaps with the grid
        min-row (- (dec pattern-rows)) ; Allow pattern to start with only bottom char visible
        max-row (dec grid-rows) ; Allow pattern to start with only top char visible
        min-col (- (dec pattern-cols)) ; Allow pattern to start with only rightmost char visible
        max-col (dec grid-cols)] ; Allow pattern to start with only leftmost char visible
    ;; Allow patterns to start at any position in and around the grid
    (for [row (range min-row (inc max-row))
          col (range min-col (inc max-col))
          :when (pattern-matches? grid pattern row col)]
      [row col])))

(defn print-grid-with-patterns
  [grid pattern]
  (let [matches (find-pattern grid pattern)
        pattern-rows (count pattern)
        pattern-cols (count (first pattern))]
    (doseq [row (range (count grid))]
      (doseq [col (range (count (first grid)))]
        (let [cell-value (get-in grid [row col])
              is-pattern-start (some #(= % [row col]) matches)
              is-in-pattern (some #(and (<= row (+ (first %) pattern-rows -1))
                                        (<= col (+ (second %) pattern-cols -1))
                                        (>= row (first %))
                                        (>= col (second %))) matches)]
          (print (cond
                   is-pattern-start "["
                   is-in-pattern "|"
                   :else " ")
                 cell-value
                 (cond
                   is-pattern-start "]"
                   is-in-pattern "|"
                   :else " "))))
      (println))))