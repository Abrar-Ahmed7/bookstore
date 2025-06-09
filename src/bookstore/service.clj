(ns bookstore.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :refer [interceptor]]
            [clojure.data.json :as json]
            [bookstore.db :as db]))


(def json-body
  "Interceptor to parse JSON request body as clojure map"
  (interceptor
   {:name :json-body
    :enter
    (fn [context]
      (let [request (:request context)]
        (try
          (let [body-string (slurp (:body request))
                json-data (when (not-empty body-string)
                            (json/read-str body-string :key-fn keyword))
                query (:query-params request)]
            (prn query)
            (assoc-in context [:request :json-params] json-data))
          (catch Exception _e
            (assoc context :response
                   {:status 400
                    :headers {"Content-Type" "application/json"}
                    :body (json/write-str {:error "Invalid JSON"})})))))}))


(def json-response
  "Interceptor to serialize response data as JSON"
  (interceptor
   {:name :json-response
    :leave (fn [context]
             (let [response (:response context)]
               (if (and response
                        (not (string? (:body response)))
                        (not (nil? (:body response))))
                 (-> context
                     (assoc-in [:response :headers "Content-Type"]
                               "application/json")
                     (update-in [:response :body] json/write-str))
                 context)))}))


(defn ok-response
  [data]
  {:status 200 :body data})


(defn created-response
  [data]
  {:status 201 :body data})


(defn not-found-response
  [message]
  {:status 404 :body {:error message}})


(defn bad-request-response
  [message]
  {:status 400 :body {:error message}})


(defn no-content-response
  []
  {:status 204 :body ""})


(defn list-books
  [_request]
  (ok-response (db/list-books)))


(defn get-book
  [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (if-let [book (db/get-book id)]
      (ok-response book)
      (not-found-response "Book not found"))))


(defn create-book!
  [request]
  (try
    (let [book-data (:json-params request)
          required-fields [:title :author :price :isbn]]
      (if (and book-data (every? book-data required-fields))
        (let [new-book (db/add-book! book-data)]
          (created-response new-book))
        (bad-request-response "Missing required fields: title, author, price, isbn")))
    (catch IllegalArgumentException e
      (bad-request-response (.getMessage e)))))


(defn update-book!
  [request]
  (try
    (let [id (parse-long (get-in request [:path-params :id]))
          book-data (:json-params request)]
      (if book-data
        (if-let [updated-book (db/update-book! id book-data)]
          (ok-response updated-book)
          (not-found-response "Book not found"))
        (bad-request-response "No JSON data provided")))
    (catch IllegalArgumentException e
      (bad-request-response (.getMessage e)))))


(defn update-book-price! [request]
  (try
    (let [title (get-in request [:query-params :title])
          price (get-in request [:json-params :price])]
      (if price
        (if-let [updated-book (db/update-book-price! {:title title
                                                      :price price})]
          (ok-response updated-book)
          (not-found-response "Book not found"))
        (bad-request-response "No JSON data provided")))
    (catch IllegalArgumentException e
      (bad-request-response (.getMessage e)))))


(defn delete-book!
  [request]
  (let [id (parse-long (get-in request [:path-params :id]))]
    (if (db/delete-book! id)
      (no-content-response)
      (not-found-response "Book not found"))))


(def common-interceptors [json-body json-response])


(def routes
  (route/expand-routes
   #{["/book"     :get    (conj common-interceptors list-books)         :route-name :list-books]
     ["/book"     :post   (conj common-interceptors create-book!)       :route-name :create-book]
     ["/book"     :patch  (conj common-interceptors update-book-price!) :route-name :update-price]
     ["/book/:id" :get    (conj common-interceptors get-book)           :route-name :get-book]
     ["/book/:id" :put    (conj common-interceptors update-book!)       :route-name :update-book]
     ["/book/:id" :delete (conj common-interceptors delete-book!)       :route-name :delete-book]}))


;; Service configuration
(def service
  {:env :prod
   ::http/routes routes
   ::http/resource-path "/public"
   ::http/type :jetty
   ::http/port 8088
   ::http/container-options {:h2c? true
                             :h2? false
                             :ssl? false}})

