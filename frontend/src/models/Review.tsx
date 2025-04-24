import User from "./User";

export default interface Review {
    id: number;
    text?: string;
    likes?: number;
    rating?: number;
    book_id?: number;
    user_id?: number;
    user?: User;
    likedUsers?: User[];
}
