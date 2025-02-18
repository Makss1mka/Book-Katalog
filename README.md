# Book-Katalog
Distributed system for managing books. 

### Stack
Java 23
Spring Boot 3.4.2
Maven 4.0
PostgreSQL
Hibernate

### Features of book service
- Get all books with\without genres
- Get books by id\name
- Get books by author id/name
- Get books by date\rating\statuses
    For all listed selection can be used pagination and sorting params

- Add book (to create need: author_id, name, file, genres)
    (Also here issued_date, rating and statuses fields, but it generates auto,
    date generetes auto by now date, rating and statuses generates auto in 0)

- Update book data (changable data: genres)

- Delete book data

### Features of review service
- Get all revies by book id
- Get all rviews by user id

- Add reviews (to create need: name, user_id, book_id, review text, rating)
    (Also here likes field, but it generates auto in value = 0)
- Add likes on reviews (to create need: user_id, review_id)

- Delete reviews (to delete need: id)
- Delete likes from reviews (to delete need: id)

- Update review data (changable values: text, rating)

### Features of auth-service
- Log in by token \ email and password \ username and password
- Registration by username, email and password
    Password codes with BCrypt algorithm

#### All other services will be soon
