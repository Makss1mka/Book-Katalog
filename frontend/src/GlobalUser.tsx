import Book from "./models/Book";
import BookStatus from "./models/BookStatus";
import User from "./models/User"

export default class GlobalUser {

    private static user: User | undefined = undefined;
    private static token: string | null = null;

    public static isEmpty(): boolean {
        return !!this.user;
    }

    public static async regUser(name: string, email: string, password: string) {
        const url = process.env.REACT_APP_USERS_URL;

        if (!url) throw new Error("Cannot access .env file properties");

        let response;
        try {
            response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: name,
                    email: email,
                    password: password
                })
            });
        
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.user = await response.json();
            this.token = response.headers.get('Authorization');
        } catch (error) {
            console.error("Error on fetch request:", error);
        }
    }

    public static setUser(user: User) {
        this.user = user;
    }

    public static setToken(token: string) {
        this.token = token;
    }


    public static getUserId(): number | undefined {
        return this?.user?.id;
    }

    public static getUserName(): string | undefined {
        return this?.user?.name;
    }

    public static getUserEmail(): string | undefined {
        return this?.user?.email;
    }

    public static getToken(): string | null {
        return this?.token;
    }

    public static getUser(): User | undefined {
        return this?.user;
    }

    public static getStatusesBooks(): BookStatus[] | undefined {
        return this?.user?.bookStatuses;
    }
    
    public static getLikedBooks(): Book[] | undefined {
        return this?.user?.likedBooks;
    }

    private GlobalUser() {}

}
