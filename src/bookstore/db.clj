(ns bookstore.db
  (:gen-class)
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.util :as mu]))


(def TitleAndPrice
  [:map
   [:title :string]
   [:price number?]])


(def Book
  "Spec for creating a book."
  (mu/merge [:map
             [:author :string]
             [:isbn :string]
             [:description {:optional true} :string]]
            TitleAndPrice))


(defn valid-params?
  "Validates book data for the given spec and returns true if the
  book is valid. Throws an exception if invalid."
  [book spec]
  (let [explanation (m/explain spec book)]
    (when-not (m/validate spec book)
      (throw (IllegalArgumentException.
              (str "Book data is invalid: "
                   (me/humanize explanation)))))
    true))


;; In-memory book store
(def books-db (atom {}))


(defn get-next-id []
  (inc (count @books-db)))


; Book management functions
(defn add-book!
  [book]
  (valid-params? book Book)
  (let [id (get-next-id)
        new-book (assoc book :id id)]
    (swap! books-db assoc id new-book)
    new-book))


(defn get-book
  "Returns the book of the given id."
  [id]
  (get @books-db id))


(defn list-books
  "Returns the list of books"
  []
  (vals @books-db))


(defn update-book!
  "Updates the book and returns the updated book"
  [id book-data]
  (let [book (cond-> (get @books-db id)
               (valid-params? book-data Book)
               (merge book-data))]
    (swap! books-db assoc id book)
    book))


(defn delete-book!
  "Deletes the book with given ID and returns true on
  success."
  [id]
  (when (get @books-db id)
    (swap! books-db dissoc id)
    true))


(defn find-book
  "Given the title of the book returns the first match."
  [title]
  (->> (list-books)
       (filter #(= (:title %) title))
       first))


(defn update-book-price!
  "Updates the book price returns the updated book data."
  [{:keys [title price] :as params}]
  (valid-params? params TitleAndPrice)
  (let [{id :id :as book} (find-book title)
        updated-book (assoc book :price price)]
    ;; This is to make sure that the queried book is present in db
    (when id
      (swap! books-db assoc id updated-book)
      updated-book)))


;; Initialize with some sample data
(defn seed-sample-data []
  (add-book! {:title "The Clojure Programming Language"
             :author "Chas Emerick"
             :price 49.99
             :isbn "978-1449394707"
             :description "Comprehensive guide to Clojure programming"})
  (add-book! {:title "Living Clojure"
             :author "Carin Meier"
             :price 39.99
             :isbn "978-1491909270"
             :description "An introduction to functional programming with Clojure"})
  (add-book! {:title "Web Development with Clojure"
             :author "Dmitri Sotnikov"
             :price 44.99
             :isbn "978-1680500820"
             :description "Build bulletproof web apps with Clojure"}))

(seed-sample-data)
