import User from "../models/User";

export async function getUserById(id: number): Promise<User | number> {
    const url = process.env.REACT_APP_USERS_URL;

    if (!url) throw new Error("Cannot access .env file properties");

    let response;
    try {
        response = await fetch(url + `/${id}?joinMode=with_statuses_and_books`);
    
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    
        let user: User = await response.json();

        if (user) {
            console.log(user);
            console.log(user.bookStatuses);
            console.log(user.likedBooks);
        }

        return user;
    } catch (error) {
        console.error("Error on fetch request:", error);

        return (response === undefined) ? 500 : response.status;
    }
} 

