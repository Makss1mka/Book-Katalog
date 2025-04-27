import './CatalogPage.css';
import { useEffect, useState } from 'react';
import { getAllBooks } from "../../api/BooksApi";
import Book from "../../models/Book";
import BookCard from "../../utils/BookCard/BookCard";
import SearchBar from '../../utils/SearchBar/SearchBar';

interface CatalogPageProps {
    genres?: string | null;
    keyWords?: string | null;
}

export default function CatalogPage({ keyWords = null, genres = null }: CatalogPageProps) {
    const [books, setBooks] = useState<Book[] | undefined>(undefined);
    const [error, setError] = useState<string | null>(null);
    const [pageNum, setPageNum] = useState<number>(0);

    useEffect(() => {
        async function fetchBooks() {
            
            let response: number | Book[];
            try {
                response = await getAllBooks(keyWords, genres);

                if (typeof response === "number") {
                    switch (response) {
                        case 400: {
                            setError("Неверные параметры поиска.");
                            break;
                        }
                        case 403: {
                            setError("У вас нет прав.");
                            break;
                        }
                        case 404: {
                            setError("Не найдено.");
                            break;
                        }
                        default: {
                            setError("Упс, какая-то ошибка.");
                        }
                    }

                    return;
                }

                setBooks(response);
            } catch (err) {
                console.error("Cannot load books", err);
                
                setError("Упс, какая-то ошибка.");
            }
        }
        fetchBooks();
    }, []); 

    const handleBooksLoad = async () => {
        try {
            const newBooks = await getAllBooks(keyWords, genres, pageNum + 1, 20);

            if (typeof newBooks == "number") {
                switch (newBooks) {
                    case 400: {
                        setError("Неверное значение номера или размера страницы отзывов.");
                        break;
                    }
                    case 403: {
                        setError("У вас нет прав.");
                        break;
                    }
                    case 404: {
                        setError("Не найдено.");
                        break;
                    }
                    default: {
                        setError("Упс, какая-то ошибка.");
                    }
                }
    
                return;
            } 
            
            if (newBooks.length) {
                setBooks(prev => [...(prev || []), ...newBooks]);
                setPageNum(prev => prev + 1);
            }
        } catch (err) {
            setError("Ошибка загрузки отзывов");
        }
    };

    if (error) {
        return <div className="CatalogPage">Error: {error}</div>;
    }

    if (!books) {
        return <div className="CatalogPage">Loading...</div>;
    }

    return (
        <div className="CatalogPage">
            <SearchBar />
            <div className="CatalogPage_BookCardsList">
                {books.map((book, index) => (
                    <BookCard key={index} book={book} />
                ))}
            </div>
            <div className="CatalogPage_LoadButton_Outer">
                <button className="CatalogPage_LoadButton" onClick={ handleBooksLoad }>Еще...</button>
            </div>
        </div>
    );
}
