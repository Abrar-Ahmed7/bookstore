(defproject bookstore "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [io.pedestal/pedestal.service "0.6.3"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.6.3"]
                 ;; [io.pedestal/pedestal.immutant "0.6.3"]
                 ;; [io.pedestal/pedestal.tomcat "0.6.3"]

                 [ch.qos.logback/logback-classic "1.2.10" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.35"]
                 [org.slf4j/jcl-over-slf4j "1.7.35"]
                 [org.slf4j/log4j-over-slf4j "1.7.35"]
                 [camel-snake-kebab "0.4.3"]
                 [org.clojure/data.json "2.4.0"]
                 [metosin/malli "0.18.0"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "bookstore.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.6.3"]]}
             :uberjar {:aot [bookstore.server]}}
  :main ^{:skip-aot true} bookstore.server)
