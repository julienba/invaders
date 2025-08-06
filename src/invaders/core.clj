(ns invaders.core
  (:require [clojure.string :as string]
            [bling.core :as bling]))

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
        ;; Allow patterns to start outside the grid
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

(defn rich-print
  [& args]
  (print (apply bling/bling args)))

(def colors
  [:red :orange :yellow :olive :green :blue :purple :magenta :gray])

(defn- indexed-by-color [xs]
  (into [] (map-indexed (fn [idx x]
                          [(nth colors (mod idx (count colors))) x])
                        xs)))

(defn print-radar-with-invaders
  "Helper for printing the grid with color pattern locations highlighted, showing complete patterns even when they extend beyond the grid.
   Colors are working when used in the terminal (ie. not in the REPL)"
  [grid pattern]
  (let [matches (find-pattern grid pattern)
        pattern-rows (count pattern)
        pattern-cols (count (first pattern))
        grid-rows (count grid)
        grid-cols (count (first grid))
        ;; Calculate extended display bounds to show complete patterns
        all-pattern-positions (for [match matches
                                    r (range pattern-rows)
                                    c (range pattern-cols)]
                                [(+ (first match) r) (+ (second match) c)])
        all-rows (concat (range grid-rows) (map first all-pattern-positions))
        all-cols (concat (range grid-cols) (map second all-pattern-positions))
        min-row (apply min all-rows)
        max-row (apply max all-rows)
        min-col (apply min all-cols)
        max-col (apply max all-cols)]

    ;; Print search invader
    (println "For invader:")
    (doseq [row (range pattern-cols)]
      (doseq [col (range pattern-rows)]
        (print (str " " (get-in pattern [row col] " ") " ")))
      (println))
    (println)
    (print (format "Found %d matches: " (count matches)))
    (doseq [[color match] (indexed-by-color matches)]
      (rich-print [color match] " "))
    (println)

    ;; Print column headers
    (print "   ")
    (doseq [col (range min-col (inc max-col))]
      (print (format "%2d " col)))
    (println)

    ;; Print each row
    (doseq [row (range min-row (inc max-row))]
      (print (format "%2d " row))
      (doseq [col (range min-col (inc max-col))]
        (let [;; Check if this position is within the original grid
              in-original-grid? (and (>= row 0) (< row grid-rows)
                                     (>= col 0) (< col grid-cols))
              original-cell-value (when in-original-grid? (get-in grid [row col]))

              ;; Check if this position is the start of any pattern
              is-pattern-start? (some #(= % [row col]) matches)

              ;; Check if this position is part of any pattern and get the pattern value
              ;; Only the first pattern found is returned
              pattern-info (first (for [[color match] (indexed-by-color matches)
                                        :let [match-row (first match)
                                              match-col (second match)
                                              pattern-r (- row match-row)
                                              pattern-c (- col match-col)]
                                        :when (and (>= pattern-r 0) (< pattern-r pattern-rows)
                                                   (>= pattern-c 0) (< pattern-c pattern-cols))]
                                    {:pattern-value (get-in pattern [pattern-r pattern-c])
                                     :match-start [match-row match-col]
                                     :color color}))
              display-char (cond
                             pattern-info (:pattern-value pattern-info)
                             in-original-grid? original-cell-value
                             :else \space)]
          (rich-print
           (if (or is-pattern-start?
                   pattern-info)
             [(:color pattern-info) (str " " display-char " ")]
             (str " " display-char " ")))))
      (println))))

