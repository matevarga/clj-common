(defproject io.glia/clj-common "0.1.0-SNAPSHOT"
  gi:license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"],
                 [commons-io/commons-io "2.4"],
                 [org.clojure/tools.logging "0.3.0"]
                 [com.taoensso/timbre "3.2.1"]
                 [org.clojure/data.csv "0.1.2"]
                 [midje "1.6.3"]]
  :plugins [[lein-midje "3.1.3"]]
  )
