import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import GlobalUser from './GlobalUser';
import { getUserById } from './api/UserApi';
import User from './models/User';

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);

let response: User | number;

(async () => {
    response = await getUserById(17);

    if (typeof response == "number") {
        console.log("Cannot load user");
        return;
    }

    GlobalUser.setUser(response);
}) ()

root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
