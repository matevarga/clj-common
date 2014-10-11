(ns glia.sys
    (:use glia.log glia.util))

(defn adjust-users
  [sys f]
  (send-off sys #(assoc %1 :users (f (%1 :users)))))

(defn use-sys [sys] (adjust-users sys inc))

(defn release-sys [sys] (adjust-users sys dec))

(defn try-start
  [sys]
  ; list of conditions req'd to start
  (if
      (and
        (> (sys :users) 0)
        (every? (fn [d] (= :started (@d :state))) (sys :dependencies))
        (= :stopped (sys :state)))

    (assoc sys :state (eval (sys :start)))
    sys))

(defn try-stop
  [sys]
  (if
      (and
        (or
          (= (sys :users) 0)
          (not-every? (fn [d] (= :started (@d :state))) (sys :dependencies))
          ) (= :started (sys :state)))
    (let [new-sys (assoc sys :state (eval (sys :stop)))]
      (when (= (new-sys :state) :stopped) (doseq [dep (new-sys :dependencies)](release-sys dep)))
      new-sys
      )
    sys))

(defn check-sanity
  [sys]
  (if
      (and
        sys
        (sys :name)
        (sys :users)
        (or (= (sys :state) :started) (= (sys :state) :stopped)))
    true
    ((error "validation failed for new state: " sys) false)))

(defn create-sys
  [name start stop & dependencies]
  (let [a (agent {
                   :name name,
                   :state :stopped,
                   :start start,
                   :stop stop,
                   :users 0,
                   ; this flattening trick is needed because of a suspected clj bug, see http://dev.clojure.org/jira/browse/CLJS-383
                   :dependencies (if (= dependencies (list nil)) nil (flatten dependencies))})]
    ; try to start or stop whenever something changes in the state
    (add-watch a :try-start-or-stop
               (fn [k r o n]
                 (when-not (= o n)
                   (send-off a try-start) (send-off a try-stop))))
    ; try to start or stop whenever something changes in the state of dependencies
    (if-let [deps (@a :dependencies)]
      (doseq [d deps]
        (add-watch d (@a :name)
                   (fn [k r o n]
                     (when-not (= o n)
                       (send-off a try-start) (send-off a try-stop))))))
    ; increase :users count of dependencies when this users of this becomes greater-than-zero
    (add-watch a :increase-dependencies-users
               (fn [k r o n]
                 (when  (and (= 0 (o :users)) (= 1 (n :users)))
                   (doseq [dep (o :dependencies)](use-sys dep)))))
    (set-validator! a check-sanity)
    (set-error-handler! a #(error %1 (full-error-trace %2)))
    a))