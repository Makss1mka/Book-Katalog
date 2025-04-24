import './FavouritePage.css';
import React, { useEffect, useState } from 'react';
import { getAllBooks } from "../../api/BooksApi";
import Book from "../../models/Book";
import BookCard from "../../utils/BookCard/BookCard";
import GlobalUser from '../../GlobalUser';
import BookStatus from '../../models/BookStatus';

export default function FavouritePage() {
    const [books, setBooks] = useState<Book[] | undefined>(undefined);

    useEffect(() => {
        if (!GlobalUser.getUser()) {
            console.log("Cannot load favourite. User is undefined.");
            return;
        }

        let booksStatuses: BookStatus[] | undefined = GlobalUser.getStatusesBooks();

        if (booksStatuses === undefined) {
            console.log("Cannot load statused books. Books is undefined.");
            return;
        }

        let books: Book[] = new Array(booksStatuses.length);

        booksStatuses.forEach(bookStatus => {
            if (bookStatus.status === "READ") {
                books.push(bookStatus.book)
            }
        });

        setBooks(books);
    }, []); 

    const handleLikePressed = () => {
        if (!GlobalUser.getUser()) {
            console.log("Cannot load favourite. User is undefined.");
            return;
        }

        let books: Book[] | undefined = GlobalUser.getLikedBooks();

        if (books === undefined) {
            console.log("Cannot load liked books. Like books is undefined");
            return;
        }

        setBooks(books);
    }

    const handleReadPressed = () => {
        if (!GlobalUser.getUser()) {
            console.log("Cannot load favourite. User is undefined.");
            return;
        }

        let booksStatuses: BookStatus[] | undefined = GlobalUser.getStatusesBooks();

        if (booksStatuses === undefined) {
            console.log("Cannot load statused books. Books is undefined.");
            return;
        }

        let books: Book[] = new Array(booksStatuses.length);

        booksStatuses.forEach(bookStatus => {
            if (bookStatus.status === "READ") {
                books.push(bookStatus.book)
            }
        });

        setBooks(books);
    }

    const handleReadingPressed = () => {
        if (!GlobalUser.getUser()) {
            console.log("Cannot load favourite. User is undefined.");
            return;
        }

        let booksStatuses: BookStatus[] | undefined = GlobalUser.getStatusesBooks();

        if (booksStatuses === undefined) {
            console.log("Cannot load statused books. Books is undefined.");
            return;
        }

        let books: Book[] = new Array(booksStatuses.length);

        booksStatuses.forEach(bookStatus => {
            if (bookStatus.status === "READING") {
                books.push(bookStatus.book)
            }
        });

        setBooks(books);
    }

    const handleDropPressed = () => {
        if (!GlobalUser.getUser()) {
            console.log("Cannot load favourite. User is undefined.");
            return;
        }

        let booksStatuses: BookStatus[] | undefined = GlobalUser.getStatusesBooks();

        if (booksStatuses === undefined) {
            console.log("Cannot load statused books. Books is undefined.");
            return;
        }

        let books: Book[] = new Array(booksStatuses.length);

        booksStatuses.forEach(bookStatus => {
            if (bookStatus.status === "DROP") {
                books.push(bookStatus.book)
            }
        });

        setBooks(books);
    }

    if (!books) {
        return <div className="FavouritePage">Loading...</div>;
    }

    return (
        <div className="FavouritePage">
            <div className="FavouritePage_SwitchButtons">
                <button className="FavouritePage_SwitchButton" onClick={ handleLikePressed }>ЛАЙКАЛ</button>
                <button className="FavouritePage_SwitchButton" onClick={ handleReadPressed }>ЧИТАЮ</button>
                <button className="FavouritePage_SwitchButton" onClick={ handleReadingPressed }>ПРОЧИТАНО</button>
                <button className="FavouritePage_SwitchButton" onClick={ handleDropPressed }>БРОСИЛ ЧИТАТЬ</button>
            </div>
            <div className="FavouritePage_BookCardsList">
                {books.map((book, index) => (
                    <BookCard key={index} book={book} />
                ))}
            </div>
        </div>
    );
}
