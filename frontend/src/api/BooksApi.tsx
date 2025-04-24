import GlobalUser from "../GlobalUser";
import Book from "../models/Book";

export async function getAllBooks(keyWords: string | null = null, genres: string | null = null): Promise<Book[] | number> {
    const url = process.env.REACT_APP_BOOKS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let queryLine: String = "";

    if (keyWords) {
        queryLine += `keyWords=${keyWords}&`;
    }

    if (genres) {
        queryLine += `genres=${genres}&`;
    }

    console.log(url + "/search?" + queryLine);

    let response;
    try {
        response = await fetch(url + "/search?" + queryLine + `_=${Date.now()}`);
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    
        const books: Book[] = await response.json();

        console.log(books);

        return books;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
} 


export async function addLikeToBook(book: Book): Promise<number> {
    const url = process.env.REACT_APP_USERS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + `/${GlobalUser.getUserId()}/like`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                bookId: book.id,
            })
        });
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return 200;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
} 

export async function deleteLikeFromBook(book: Book): Promise<number> {
    const url = process.env.REACT_APP_USERS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + `/${GlobalUser.getUserId()}/like?toBook=${book.id}`, {
            method: 'DELETE'
        });
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return 200;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
} 


