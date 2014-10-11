(ns glia.sys-test
  (:use glia.sys midje.sweet glia.util ))

(def test-log (atom ()))

(defn add-trace [v] (swap! test-log conj v))

(defn reset-trace [] (reset! test-log ()))

(defn create-test-sys [x tstart tstop & dependencies]
  (create-sys x
              `(do
                 (add-trace ~tstart)
                 :started
                 )
              `(do
                 (add-trace ~tstop)
                 :stopped
                 )
              dependencies))


(fact "sys can be started asynchronously"
      (let [sys2 (create-test-sys :s1 :start1 :stop1)]
        (use-sys sys2)
        (Thread/sleep 3000)
        (try
          (await sys2) (catch Exception e))
        (if-let [error (agent-error sys2)]
          (do
            (full-error-trace error)
            (dbg @sys2)
            (dbg (.getStackTrace error))
            (dbg error)
            ))
        (@sys2 :state)
        ) => :started
      )

(fact "sys start starts its dependencies first"
      (let [sys2 (create-test-sys :s2 :start2 :stop2)]
        (let [sys3 (create-test-sys :s3 :start3 :stop3 sys2)]
          (reset-trace)
          (use-sys sys3)
          (Thread/sleep 500)
          (await-for 1000 sys3)
          (if-let [error (agent-error sys3)]
            (do
              (full-error-trace error)
              (dbg @sys3)
              (dbg (.getStackTrace error))
              (dbg error)
              ))
          (await sys2)
          @test-log
          )) => (list :start3 :start2)
      )

(fact "sys stop stops its first then the dependencies"
      (let [sys4 (create-test-sys :s4 :start4 :stop4)]
        (let [sys5 (create-test-sys :s4 :start5 :stop5 sys4)]
          (reset-trace)
          (use-sys sys5)
          (Thread/sleep 500)
          (release-sys sys5)
          (Thread/sleep 500)
          @test-log)) => (list :stop4 :stop5 :start5 :start4))

