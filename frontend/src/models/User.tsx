import Book from "./Book";
import BookStatus from "./BookStatus";

export default interface User {
    id: number;
    name: string;
    email: string;
    bookStatuses?: BookStatus[];
    likedBooks?: Book[];
}
