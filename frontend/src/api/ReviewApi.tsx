import GlobalUser from "../GlobalUser";
import Book from "../models/Book";
import Review from "../models/Review";

export async function getAllBookReviews(book: Book, pageNum: number, pageSize: number): Promise<Review[] | number> {
    const url = process.env.REACT_APP_REVIEWS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + `?id=${book.id}&criteria=bookId&pageSize=${pageSize}&pageNum=${pageNum}&joinMode=with`);
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    
        const reviews: Review[] = await response.json();
        return reviews;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
} 


export async function addReview(text: string, rating: number, book: Book): Promise<Review | number> {
    const url = process.env.REACT_APP_REVIEWS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: GlobalUser.getUserId(),
                bookId: book.id,
                text: text,
                rating: rating
            })
        });
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const newReview: Review = await response.json();
        return newReview;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
}

export async function addLikeToReview(review: Review): Promise<number> {
    const url = process.env.REACT_APP_REVIEWS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + '/like', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: GlobalUser.getUserId(),
                reviewId: review.id
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


export async function deleteReview(review: Review): Promise<number> {
    const url = process.env.REACT_APP_REVIEWS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + `/${review.id}`, {
            method: 'DELETE',
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

export async function deleteLikeFromReview(review: Review): Promise<number> {
    const url = process.env.REACT_APP_REVIEWS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + `/like?fromUser=${GlobalUser.getUserId()}&toReview=${review.id}`, {
            method: 'DELETE',
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


export async function updateReview(text: string | null = null, rating: number | null = null, review: Review): Promise<Review | number> {
    const url = process.env.REACT_APP_REVIEWS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let body;

    if (text && !rating) body = { "text": text }
    if (!text && rating) body = { "rating": rating }
    if (text && rating) body = { "text": text, "rating": rating }

    let response
    try {
        response = await fetch(url + `/${review.id}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body)
        });
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const newReview: Review = await response.json();
        return newReview;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
}
