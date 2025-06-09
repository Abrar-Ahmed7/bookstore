# bookstore

## Prerequisite
1. JDK
2. Clojure (1.11.1)


## API docs:

### List books
Endpoint: /book <br>
Method: GET <br>
Sample Request:
```curl
http://localhost:8088/book

=> [
    {
        "title": "Book title",
        "author": "Book author",
        "price": 5000,
        "isbn": "978-1449394707",
        "description": "Comprehensive guide",
        "id": 1
    },
    .
    .
    .
]
```

### Get a book
Endpoint: /book/:id <br>
Method: GET <br>
Sample Request:
```curl
http://localhost:8088/book/1

=> {
        "title": "Book title",
        "author": "Book author",
        "price": 5000,
        "isbn": "978-1449394707",
        "description": "Comprehensive guide",
        "id": 1
    }
```

### Create a book
Endpoint: /book <br>
Method: POST <br>
Request body:  
```
{
   "title": "Book title",
   "author": "Book author",
   "price": 5000,
    "isbn": "978-1449394707",
   "description": "Comprehensive guide"
 }
 ``` 
Sample Request:
```curl
http://localhost:8088/book

=> {
        "title": "Book title",
        "author": "Book author",
        "price": 5000,
        "isbn": "978-1449394707",
        "description": "Comprehensive guide",
        "id": 101
    }
```


### Update a book
Endpoint: /book/:id <br>
Method: PUT <br>
Request body: 
```
{
   "title": "Book title-edited",
   "author": "Book author-updated",
   "price": 3000,
    "isbn": "978-1449394707",
   "description": "Comprehensive guide updated"
 }
 ``` 
 Sample Request:
```curl
http://localhost:8088/book/101

=> {
        "title":"Book title-edited",
        "author":"Book author-updated",
        "price": 3000,
        "isbn": "978-1449394707",
        "description":"Comprehensive guide updated",
        "id": 101
    }
```

### Update the book price
Endpoint: /book?title=book-title <br>
Method: PATCH <br>
Request body: 
```
{
   "price" : 2345
 } 
 ``` 
 Sample Request:
```curl
http://localhost:8088/book?title=book-title

=> {
        "title":"Book title-edited",
        "author":"Book author-updated",
        "price": 2345,
        "isbn": "978-1449394707",
        "description":"Comprehensive guide updated",
        "id": 101
    } 
```

### Delete a book
Endpoint: /book <br>
Method: DELETE <br> 
 Sample Request:
```curl
http://localhost:8088/book/101

=> 204 Response code
```

## Getting Started

1. Start the application: `lein run`
1. Go to [localhost:8088](http://localhost:8088/) and start hitting the APIs based on the API docs
1. Refer source code at src/bookstore/service.clj. Explore the docs of 
   functions that define routes and responses.
1. Run the app's tests with `lein test`. 
1. To run specifice test function, example: `lein test :only bookstore.service-test/list-books-test`
1. Read the tests at test/bookstore/service_test.clj.


## Future Enhancements:
- Use any sql db to store the normalized version of the book store
- Use `next-jdbc` for JDBC wrapper
- Use `migratus` library for sql migration
- Use `camel-snake-kabab` library for transforming keys and json serialization
- Use `integrant` to initialize the app and get the system config. Pass the system config via interceptor to context so that all request can use the config in their function signatures.
- Introduce endpoint for users
- Add Authentication and RBAC for readers and publishers
- Introduce `koacha` testing library
- If possible, use XTDB v1 using RocksDB
   - Then, find a way to migrate from RocksDB to JDBC for connection pooling (RockDB uses single connection which is way hard for scaling when requests/events piles up)
   - Then, Migrate from v1 to v2 (Try to create better blog for this)




