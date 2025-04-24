import ReactDOM from 'react-dom/client';
import Book from "../../models/Book"
import './BookCard.css';
import BookPage from "../../pages/BookPage/BookPage";

interface BookCardProps {
    book: Book;
}

export default function BookCard({ book }: BookCardProps) {
    const genres = (book.genres) 
        ? book.genres.join(", ") 
        : undefined;
    const rating = (book.rating !== undefined)
        ? Math.min(Math.max(Math.round(book.rating), 0), 5)
        : -1;
    const issued_date = (book.issued_date)
        ? new Date(book.issued_date).toLocaleDateString()
        : undefined;

    const handleCardClicked = () => {
        let main = document.getElementById('main');

        if (!main) return;

        ReactDOM.createRoot(main).render(
            <BookPage book={ book } />
        );
    }

    console.log(rating);

    return (
        <div className="BookCard" onClick={ handleCardClicked }>
            <label className="BookCard_Name">{book.name}</label>
            <p className="BookCard_Rating">
                {
                    (rating == -1) 
                        ? undefined 
                        : <>
                            {'★'.repeat(rating)}
                            {'☆'.repeat(5 - rating)}
                            {` ${ book.rating }/5.0`}
                        </>
                }
            </p>
            <p className="BookCard_Genres" title={genres}>{genres}</p>
            <p className="BookCard_Likes">{book.likes}</p>
            <p className="BookCard_IssuedDate">{ issued_date }</p>
        </div>
    )
}
