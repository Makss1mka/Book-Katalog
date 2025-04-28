import { useState } from "react";
import GlobalUser from "../../GlobalUser";
import Review from "../../models/Review";
import { IconLikeEmpty, IconLikeFilled, IconTrashBin } from "../icons";
import "./ReviewCard.css";
import { addLikeToReview, deleteLikeFromReview, deleteReview } from "../../api/ReviewApi";
import User from "../../models/User";

interface ReviewCardProps {
    review: Review, 
    key: number
}

export default function ReviewCard({ review, key }: ReviewCardProps) {
    const [isDeleted, setIsDeleted] = useState<boolean>(false);
    const rating = (review.rating !== undefined)
        ? Math.min(Math.max(Math.round(review.rating), 0), 5)
        : -1;
    const authorName = (review.author && review.author.name)
        ? review.author.name
        : undefined;
    const [isLiked, setIsLiked] = useState<boolean>(() => {
        if (!review.likedUsers) return false;

        return review.likedUsers.some(user => user.id == GlobalUser.getUserId());
    });

    const handleLikeClick = async () => {
        if (!GlobalUser.getUser() || review.likes == null || review.likedUsers == null) {
            console.log("Cannot handle like pressing cause smth is null", GlobalUser.getUser(), review.likes, review.likedUsers)
            return;
        }

        let response: number;
        if (isLiked) {
            response = await deleteLikeFromReview(review);

            if (response != 200) {
                console.log(`Some error. ${response}.`);
                return;
            }

            setIsLiked(false);
            
            review.likes--;

            let ind = -1;
            for (let i = 0; i < review.likedUsers.length; i++) {
                if (review.likedUsers[i].id == GlobalUser.getUserId()) {
                    ind = i;
                    break;
                }
            }

            review.likedUsers.splice(ind, 1);
        } else {
            response = await await addLikeToReview(review);

            if (response != 200) {
                console.log(`Some error. ${response}.`);
                return;
            }
            
            setIsLiked(true);

            review.likes++;
            
            let user: User | undefined = GlobalUser.getUser();
            if (user != undefined) {
                review.likedUsers.push(user);
            }
        }
    };

    const handleDeleteClick = async () => {
        let author = GlobalUser.getUser()

        if (author == undefined || isDeleted) {
            console.log("Cannot handle delete pressing cause smth is null", author, isDeleted);
            return;
        }

        let response: number = await deleteReview(review);
        if (response != 200) {
            console.log(`Some error. ${response}.`);
            return;
        } else {
            setIsDeleted(true);
        }
    }

    const isUserAuthor = () => {
        let globalUserId: number | undefined = GlobalUser.getUserId();
        
        if (globalUserId == undefined || review.author == undefined || globalUserId != review.author.id) {
            return false;
        } else {
            return true;
        }
    }

    if (isDeleted) {
        return <></> 
    } else {
        return ( 
            <div className="ReviewCard" key={ key }>
                <div className="ReviewCard_UpperBlock">
                    <div className="ReviewCard_Author">{ authorName }</div>
                    {(isUserAuthor()) &&
                        <div className="ReviewCard_TrahsButton" onClick={ handleDeleteClick }>
                            <IconTrashBin className="ReviewCard_TrahsButton_Icon"/>
                        </div>
                    }
                </div>
                <div className="ReviewCard_Text">{ review.text }</div>
                <div className="ReviewCard_Rating">
                    {
                        (rating == -1)
                            ? undefined
                            : <>
                                {'★'.repeat(Math.round(rating))}
                                {'☆'.repeat(5 - Math.round(rating))}
                                {` ${ review.rating }/5.0`}
                            </>
                    }
                </div>
                <div className="ReviewCard_LikesBlock">
                    <div className="ReviewCard_Likes">{ review.likes }</div>
                    <button 
                        className="ReviewCard_LikeButton" 
                        onClick={handleLikeClick}
                    >
                        {isLiked 
                            ? <IconLikeFilled className="ReviewCard_LikeButtonIcon_Active" /> 
                            : <IconLikeEmpty className="ReviewCard_LikeButtonIcon_Inactive" />
                        }
                    </button>
                </div>
            </div>
        )
    }
}


