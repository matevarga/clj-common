(ns glia.log(:require [clojure.tools.logging :as log] [taoensso.timbre :as timbre] ))

(defn info
  [& l]
  (timbre/info l))

(defn error
  [& l]
  (timbre/error l))

(defn debug
  [& l]
  (timbre/debug l))

(defn trace
  [& l]
  (timbre/trace l))

