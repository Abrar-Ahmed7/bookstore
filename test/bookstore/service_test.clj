(ns bookstore.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as http]
            [bookstore.service :as service]
            [clojure.data.json :as json]
            [bookstore.db :as db]))

(defn service
  []
  (::http/service-fn (http/create-servlet service/service)))



(defn- parse-json
  "Parse the given Json to clojure keyword map."
  [response]
  (update response :body #(json/read-str % :key-fn keyword)))


(defn db-fixture
  [f]
  (reset! db/books-db {})
  (db/seed-sample-data)
  (f))


(use-fixtures :each db-fixture)


(deftest list-books-test
  (testing "Feature: List books"
    (let [response (-> (response-for (service) :get "/book")
                       parse-json)]
      (testing "Effect: Getting 200 response status"
        (is (= 200 (:status response))))
      (testing "Effect: Checking the number of books"
        (is (= 3 (count (:body response))))))))


(deftest get-book-test
  (testing "Feature: Get a book"
    (testing "Scenario: Book is present"
      (let [response (-> (response-for (service) :get "/book/1")
                         parse-json)]
        (testing "Effect: Getting 200 response code"
          (is (= 200 (:status response))))
        (testing "Effect: Checking if the book has returned"
          (is (= 1 (:id (:body response)))))))
    (testing "Scenario: Non-existent"
      (let [response (response-for (service) :get "/book/999")]
        (testing "Effect: Getting 404 response code"
          (is (= 404 (:status response))))))))


(deftest create-book-test
  (testing "Feature: Create a new book"
    (testing "Scenario: Creating a book with correct params"
      (let [book-data {:title "Test Book"
                       :author "Tester"
                       :price 2999
                       :isbn "123-456"}
            response (-> (response-for (service)
                                       :post "/book"
                                       :headers {"Content-Type"
                                                 "application/json"}
                                       :body (json/write-str book-data))
                         parse-json)]
        (testing "Effect: Getting 201 response code"
          (is (= 201 (:status response))))
        (testing "Effect: Returns the created book"
          (is (= "Test Book" (:title (:body response)))))
        (testing "Effect: Check if the new book got added to db"
          (is (= 4 (count (:body (-> (response-for (service) :get "/book")
                                     parse-json))))))))
    (testing "Feature: Creating book with invalid data"
      (let [;; Missing required fields
            invalid-data {:title "Invalid Book"}
            response (response-for (service)
                                   :post "/book"
                                   :headers {"Content-Type" "application/json"}
                                   :body (json/write-str invalid-data))]
        (testing "Effect: Getting error code 400"
          (is (= 400 (:status response))))))))


(deftest update-book-test
  (testing "Feature: Update a book"
    (testing "Scenario: Testing with valid data"
      (let [book-data {:title "Updated Title"
                       :author "Updated Author"
                       :price 9900
                       :isbn "updated-isbn"}
            response (-> (response-for (service)
                                       :put "/book/2"
                                       :headers {"Content-Type"
                                                 "application/json"}
                                       :body (json/write-str book-data))
                         parse-json)]
        (testing "Effect: Getting 200 response code"
          (is (= 200 (:status response))))
        (testing "Effect: Checking the updated data"
          (is (= "Updated Title" (:title (:body response))))
          (is (= 2 (:id (:body response)))))
        (testing "Effect: Checking if the count is smae"
          (is (= 3 (count (:body (-> (response-for (service) :get "/book")
                                     parse-json))))))))
    (testing "Feature: Updating book with invalid data"
      (let [;; Missing required fields
            invalid-data {:title "Invalid Book"}
            response (response-for (service)
                                   :put "/book/2"
                                   :headers {"Content-Type"
                                             "application/json"}
                                   :body (json/write-str invalid-data))]
        (testing "Effect: Getting error code 400"
          (is (= 400 (:status response))))))))


(deftest update-book-price-test
  (testing "Feature: Update the book price"
    (testing "Scenario: Updating with valid data"
      (let [price-data {:price 5645}
            title "Living Clojure"
            response (-> (response-for (service)
                                       :patch (str "/book?title=" title)
                                       :headers {"Content-Type"
                                                 "application/json"}
                                       :body (json/write-str price-data))
                         parse-json)]
        (testing "Effect: Getting 200 response code"
          (is (= 200 (:status response))))
        (testing "Effect: Checking if the data correct"
          (is (= 5645 (:price (:body response))))
          (is (= title (:title (:body response)))))))))


(deftest delete-book-test
  (testing "Feature: Delete a book"
    (testing "Scenario: Delete the actual book"
      (let [response (response-for (service) :delete "/book/3")]
        (testing "Effect: Getting 204 response code"
          (is (= 204 (:status response))))
        (testing "Effect: Check if the book got deleted"
          (let [get-response (response-for (service) :get "/book/3")]
            (is (= 404 (:status get-response)))))))
    (testing "Scenario: Trying to delete the book that is not present"
      (let [response (response-for (service) :delete "book/999")]
        (testing "Effect: Getting 404 Not found code"
          (is (= 404 (:status response)))))))) 

