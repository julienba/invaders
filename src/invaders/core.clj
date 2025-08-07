(ns invaders.core
  (:require [bling.core :as bling]
            [bling.banner :refer [banner]]
            [bling.fonts.rounded]
            [clojure.string :as string]))

(defn str->grid
  "Sanitize the input and return a 2D array.
   It will throw an exception if the grid is not legitimate (wrong char or wrong size).
   The chars allow are '-', 'o' and 'O'"
  [s]
  (when-not (string? s)
    (throw (ex-info "Wrong input" {:input s})))
  (when (string/blank? s)
    (throw (ex-info "Empty grid" {:input s})))
  (let [grid (into [] (for [line (string/split-lines s)
                            :let [line (string/trim line)
                                  _  (when-not (re-matches #"^[\-oO]*$" line)
                                       (throw (ex-info "Grid string contains invalid characters" {:input s})))]]
                        (into [] (seq (string/trim line)))))
        row-length (count (first grid))]
    (doseq [row (drop 1 grid)]
      (when-not (= row-length (count row))
        (throw (ex-info "Grid rows have different size" {:input s
                                                         :grid grid}))))
    grid))

(defn- count-rows-and-cols [grid pattern]
  {:grid-rows (count grid)
   :grid-cols (count (first grid))
   :pattern-rows (count pattern)
   :pattern-cols (count (first pattern))})

(defn- pattern-matches?
  "Check each cell in the pattern that overlaps with the grid"
  [grid pattern start-row start-col]
  (let [{:keys [grid-rows grid-cols pattern-rows pattern-cols]} (count-rows-and-cols grid pattern)]
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
  "Returns a sequence of [row col] coordinates where the pattern starts"
  [grid pattern]
  (when (or (empty? grid) (empty? pattern))
    (throw (ex-info "Empty grid or pattern" {:grid grid :pattern pattern})))
  (let [{:keys [grid-rows grid-cols pattern-rows pattern-cols]} (count-rows-and-cols grid pattern)
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

(defn- rich-print
  [& args]
  (print (apply bling/bling args)))

(def colors
  [:red :orange :yellow :olive :green :blue :purple :magenta :gray])

(defn- indexed-by-color [xs]
  (into [] (map-indexed (fn [idx x]
                          [(nth colors (mod idx (count colors))) x])
                        xs)))

(defn- print-radar-with-invaders
  "Helper for printing the grid with color pattern locations highlighted, showing complete patterns even when they extend beyond the grid.
   Colors are working when used in the terminal (ie. not in the REPL)"
  [grid pattern]
  (let [matches (find-pattern grid pattern)
        {:keys [grid-rows grid-cols pattern-rows pattern-cols]} (count-rows-and-cols grid pattern)
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
    (println "\nFor invader:")
    (doseq [row (range pattern-rows)]
      (doseq [col (range pattern-cols)]
        (print (str (get-in pattern [row col] " "))))
      (println))
    (println)

    (print (format "Found %d matches: \n" (count matches)))
    (doseq [[color match] (indexed-by-color matches)]
      (rich-print [color match] " "))
    (println)
    (println)

    ;; Print each row
    (doseq [row (range min-row (inc max-row))]
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
             [(:color pattern-info) (str "" display-char "")]
             (str "" display-char "")))))
      (println))
    (println)))

(defn file->grid [file-path]
  (str->grid (slurp file-path)))

(defn- print-big-title []
  (println (banner
            {:font               bling.fonts.rounded/rounded
             :font-weight        :bold
             :text               "Space Invaders"
             :gradient-direction :to-left
             :gradient-colors    [:cool :warm]})))

(defn- print-title [s]
  (bling/print-bling [{:background-color "purple"
                       :color            :white
                       :font-weight      :bold} s]))

(defn demo []
  (let [radar (str->grid (slurp "resources/radar/1.txt"))
        invader1 (str->grid (slurp "resources/invaders/1.txt"))
        invader2 (str->grid (slurp "resources/invaders/2.txt"))]

    (print-title "## Invader 1 ##")
    (print-radar-with-invaders radar invader1)

    (print-title "## Invader 2 ##")
    (print-radar-with-invaders radar invader2)))

(defn -main
  "This is the main entry point for the application.
  It expects two command-line arguments, which are file paths."
  [& args]
  (print-big-title)
  (if (= (count args) 2)
    (let [[radar-path invader-path] args
          radar (str->grid (slurp radar-path))
          invader (str->grid (slurp invader-path))]
      (print-radar-with-invaders radar invader))
    (println "Error: Please provide exactly two file paths as arguments.")))
