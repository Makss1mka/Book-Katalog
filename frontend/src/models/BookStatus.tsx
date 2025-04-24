import Book from "./Book";

export default interface BookStatus {
    book: Book;
    id: number;
    status: string;
}
