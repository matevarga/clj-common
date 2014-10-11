 (ns glia.util-test
   (:use glia.util midje.sweet))

(facts "about insert-at-every"
  (insert-at-every ["a" "b"] 1 1) => ["a" 1 "b"]
  (insert-at-every ["a" "b" "c"] :a 2) => ["a" "b" :a "c"])

(facts "about map utils"
  (apply-by-key {:a 1, :b 2} {:a #(str %), :b #(str %)}) => {:a "1", :b "2"}
  ((comp-map  {1 "1", 2 "2", 3 "3"} {:1 1, :2 2, :3 3}) :1) => "1")

