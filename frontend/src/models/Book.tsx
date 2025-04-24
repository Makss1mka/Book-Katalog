import User from "./User";

export default interface Book {
    id: number;
    author_id: number;
    name: string;
    file_path?: string;
    rating?: number;
    genres?: string[];
    ratings_count?: number;
    issued_date?: string;
    likes?: number;
    author?: User;
}
