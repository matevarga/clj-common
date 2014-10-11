(ns glia.util
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [glia.log :as log]
            ))

(defn tail-file
  [path handler-func]
  (log/info "Starting tailing " path)
  (org.apache.commons.io.input.Tailer/create
    (io/file path)
    (proxy [org.apache.commons.io.input.TailerListenerAdapter] []
      (handle [line]
        (handler-func line)
        ))))

(defn load-props
  [file-name]
  (with-open [^java.io.Reader reader (io/reader file-name)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [(keyword k) (read-string v)])))))

(defn insert-at-every
  [coll value n]
  (apply concat (interpose [value] (partition-all n coll))))

(defn apply-by-key
  "Given two maps, m1 (k1 -> v1) and m2 (k2 -> f2), applies f2 to v1 where k2==k1. Filters out keys in m1 that do not exist in m2."
  [map-to-values map-to-funcs]
  (let [[map-to-values] [(select-keys map-to-values (keys map-to-funcs))]]
    (into {} (for [[k v] map-to-values] [k ((k map-to-funcs) v)]))))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn comp-map
  "Composes two sequential map lookup into a function. Similar to comp, but takes two maps [f g] where (= (values f) (keys g))"
  [f g]
  (fn [k]
    (f (g k))))

(defn full-error-trace [error]
  (string/join [ (.getMessage error) " @ "(string/join " >>> "  (map (fn [x] (str x)) (.getStackTrace error)))]))

(defn pprint-str
  [x]
  (with-out-str (clojure.pprint/pprint x)))

; from https://groups.google.com/forum/#!topic/clojure/cOXClow1Wn4
(defmacro dbg
  [x]
  `(let [x# ~x]
     (printf "dbg %s:%s> %s is %s\n"
             ~*ns*
             ~(:line (meta &form))
             ~(pr-str x)
             (pprint-str x#))
     (flush)
     x#))


(defn addShutdownHook [hook]
  (.addShutdownHook (Runtime/getRuntime) (Thread. (hook))))

(defn flip
  "Example: [[1 2 3] [4 5 6]] => [[4 1] [2 5] [3 6]]"
  [seq-of-seqs]
  (cons (map first seq-of-seqs) (lazy-seq (flip (map rest seq-of-seqs)))))

