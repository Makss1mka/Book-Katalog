import React, { useEffect, useState } from 'react';
import { addReview, getAllBookReviews } from '../../api/ReviewApi';
import Book from '../../models/Book';
import './BookPage.css';
import Review from '../../models/Review';
import ReviewCard from '../../utils/ReviewCard/ReviewCard';
import GlobalUser from '../../GlobalUser';
import { addLikeToBook, deleteLikeFromBook } from '../../api/BooksApi';
import { IconFavouriteEmpty, IconFavouriteFilled, IconLikeEmpty, IconLikeFilled } from '../../utils/icons';
import BookStatus from '../../models/BookStatus';
import { Button, Dropdown, Menu } from 'antd';
import User from '../../models/User';

interface BookPageProps {
    book: Book;
}

export default function BookPage({ book }: BookPageProps) {
    const genres = (book.genres) ? book.genres.join(", ") : undefined;
    const [reviews, setReviews] = useState<Review[] | undefined>(undefined);
    const [error, setError] = useState<string | null>(null);
    const [pageReviewsNum, setPageReviewNum] = useState<number>(0);
    const [showErrOnAddReview, setShowErrOnAddReview] = useState<boolean>(false);
    const [addReviewErrText, setAddReviewErrText] = useState<string>("");
    const [isStatusMenuOpen, setIsStatusMenuOpen] = useState<boolean>(false);
    const [userStatus, setUserStatus] = useState<string>(() => {
        let statuses: BookStatus[] | undefined = GlobalUser.getStatusesBooks();
        
        if (statuses == undefined) return "";

        for (let status of statuses) {
            if (status.book.id == book.id) return status.status;
        } 

        return "";
    });
    const [isLiked, setIsLiked] = useState<boolean>(() => {
        let likedBooks: Book[] | undefined = GlobalUser.getLikedBooks();

        if (!likedBooks) return false;

        for (let likedBook of likedBooks) {
            if (book.id == likedBook.id) return true;
        }

        return false;
    });

    const bookRating = Math.min(Math.max(Math.round((book.rating) ? book.rating : -1), 0), 5);

    const handleLikeClick = async () => {
        if (!GlobalUser.getUser()) return;

        let likedBooks: Book[] | undefined = GlobalUser.getLikedBooks();
        let response: number;

        if (likedBooks == undefined) return;

        if (isLiked) {
            response = await deleteLikeFromBook(book);

            if (response != 200) {
                console.log(`Some error. ${response}.`);
                return;
            }

            setIsLiked(false);

            if (book.likes !== undefined) book.likes--;

            let ind = -1;
            for (let i = 0; i < likedBooks.length; i++) {
                if (likedBooks[i].id == book.id) {
                    ind = i;
                    break;
                }
            }

            likedBooks.splice(ind, 1);
        } else {
            response = await addLikeToBook(book);

            if (response != 200) {
                console.log(`Some error. ${response}.`);
                return;
            }    

            setIsLiked(true);

            if (book.likes !== undefined) book.likes++;

            likedBooks.push(book);
        }
    };

    const handleReviewsLoad = async () => {
        try {
            const newReviews = await getAllBookReviews(book, pageReviewsNum + 1, 5);

            if (typeof newReviews == "number") {
                switch (newReviews) {
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
            
            if (newReviews.length) {
                setReviews(prev => [...(prev || []), ...newReviews]);
                setPageReviewNum(prev => prev + 1);
            }
        } catch (err) {
            setError("Ошибка загрузки отзывов");
        }
    };

    const handleAddReview = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        let ratingInput = (event.currentTarget.elements.namedItem("rating") as HTMLInputElement);

        let rating: number | undefined;
        if (!ratingInput || (rating = parseInt(ratingInput.value, 10)) === undefined || isNaN(rating)) {
            console.log("Cannot parse int:", rating);
            
            setShowErrOnAddReview(true);
            setAddReviewErrText("Выберите значение рейтинга.");

            return;
        }

        const textInput = (event.currentTarget.elements.namedItem("reviewText") as HTMLInputElement);
        const text = textInput ? textInput.value : "";

        console.log("Рейтинг отзыва:", rating);
        console.log("Текст отзыва:", text);

        let review: Review | number = await addReview(text, rating, book);

        if (typeof review === "number") {
            setShowErrOnAddReview(true);

            switch (review) {
                case 400: {
                    setAddReviewErrText("Неверное значение рейтинга или текста.");
                    break;
                }
                case 403: {
                    setAddReviewErrText("У вас нет прав.");
                    break;
                }
                case 404: {
                    setAddReviewErrText("Не найден обработчик.");
                    break;
                }
                case 409: {
                    setAddReviewErrText("Вы уже добавляли отзыв на эту книгу.");
                    break;
                }
                default: {
                    setAddReviewErrText("Упс, какая-то ошибка.");
                }
            }

            return;
        } else {
            setReviews(prevReviews => {
                if (prevReviews === undefined || typeof review === "number") return undefined;
    
                const newReviews = [...prevReviews];
                newReviews.unshift(review);
                return newReviews;
            });
        }
    };

    // const handleReadingStatusClicked = async () => {
    //     let user: User | undefined = GlobalUser.getUser();
    
    //     if (user == undefined || userStatus == "reading") return;
    
    //     let response: number = await 
    
    
    // }

    useEffect(() => {
        (async () => {
            try {
                let fetchedReviews: Review[] | number = await getAllBookReviews(book, pageReviewsNum, 5);

                if (typeof fetchedReviews == "number") {
                    switch (fetchedReviews) {
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

                setReviews(fetchedReviews);
            } catch (err) {
                console.error("Cannot load reviews", err);

                setError("Failed to load reviews.");
            }
        })();
    }, []);

    if (error) {
        return <div className="BookPage">Error: {error}</div>;
    }

    if (!reviews) {
        return <div className="BookPage">Loading...</div>;
    }

    // const statusMenu = [
    //     { key: "1", label: "Читаю", onclick={ handleReadingStatusClicked } },
    //     { key: "2", label: "Прочитал", onclick={ handleReadStatusClicked } },
    //     { key: "3", label: "Бросил читать", onclick={ handleDropStatusClicked } },
    // ];

    // <Dropdown menu={{ items: statusMenu }} trigger={['click']}>
    //                 <Button>
    //                     {userStatus == "" 
    //                         ? <IconFavouriteEmpty className='BookPage_UpperBlock_StatusIcon'/>
    //                         : <IconFavouriteFilled className='BookPage_UpperBlock_StatusIcon'/>
    //                     }
    //                 </Button>
    //             </Dropdown>

    return (
        <div className="BookPage">
            <div className="BookPage_UpperBlock">
                <label className='BookPage_NameLabel'>{book.name}</label>
            </div>
            {
                (book.author)
                    ? <p className='BookPage_Author'>{book.author.name}</p>
                    : <></>
            }
            <p className='BookPage_Rating'>
                {'★'.repeat(bookRating)}
                {'☆'.repeat(5 - bookRating)}
                {` ${bookRating.toFixed(2)}/5.0`}
            </p>
            <p className='BookPage_RatingsCount'>{book.ratings_count}</p>
            <p className='BookPage_Genres'>{genres}</p>
            <div className='BookPage_LikeBlock'>
                <p className='BookPage_Likes'>{book.likes}</p>
                <button
                    className="BookPage_LikeButton"
                    onClick={ handleLikeClick }
                >
                    {isLiked
                        ? <IconLikeFilled className="BookPage_LikeButtonIcon_Active" />
                        : <IconLikeEmpty className="BookPage_LikeButtonIcon_Inactive" />
                    }
                </button>
            </div>
            <div className='BookPage_ReviewCreateForm'>
                <form onSubmit={handleAddReview}>
                    <div className='BookPage_ReviewCreateForm_RatingSelector'>
                        <p>Выберите рейтинг:</p>
                        {[1, 2, 3, 4, 5].map(star => (
                            <label key={star}>
                                <input
                                    type="radio"
                                    name="rating"
                                    value={star}
                                />
                                {'★'.repeat(star)}
                            </label>
                        ))}
                    </div>
                    <textarea
                        className='BookPage_ReviewCreateForm_TextInput'
                        name="reviewText"
                        placeholder="Напишите ваш отзыв"
                    />
                    <div >
                        {
                            showErrOnAddReview && (
                                <p className="BookPage_InvalidData" id="BookPage_InvalidData">{ addReviewErrText }</p>
                            )
                        }
                    </div>
                    
                    <button type="submit">Отправить</button>
                </form>
            </div>
            <div className='BookPage_ReviewsList'>
                {
                    reviews.map((review) => (
                        <ReviewCard
                            review={review}
                            key={review.id}
                        />
                    ))
                }
                <button onClick={ handleReviewsLoad }>Еще...</button>
            </div>
        </div>
    );
}

