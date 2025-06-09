(ns bookstore.server
  (:gen-class)
  (:require [io.pedestal.http :as server]
            [bookstore.service :as service]))


(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating the server...")
  (-> service/service
      server/default-interceptors
      server/create-server
      server/start))