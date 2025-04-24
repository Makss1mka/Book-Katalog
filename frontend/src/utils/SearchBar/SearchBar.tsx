import React, { useState } from 'react';
import ReactDOM from 'react-dom/client';
import './SearchBar.css';
import { IconSearch } from "../icons";
import CatalogPage from "../../pages/CatalogPage/CatalogPage";

type GenreOption = {
    id: number;
    name: string;
};

const genreOptions: GenreOption[] = [
    { id: 1, name: 'Боевик' },
    { id: 2, name: 'Фантастика' },
    { id: 3, name: 'Драма' },
    { id: 4, name: 'Комедия' },
    { id: 5, name: 'Ужасы' },
    { id: 6, name: 'Эссе' },
    { id: 7, name: 'Фольклор' },
    { id: 8, name: 'Научная фантастика' },
    { id: 9, name: 'Мемуары' },
    { id: 10, name: 'Приключения' },
    { id: 11, name: 'Юмор' },
    { id: 12, name: 'Классика' },
    { id: 13, name: 'Сказки' },
    { id: 14, name: 'Детская литература' },
    { id: 15, name: 'Биография' },
    { id: 16, name: 'Спорт' },
];

export default function SearchBar() {
    const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);
    const [selectedGenres, setSelectedGenres] = useState<string[]>([]);

    const handleSearchClicked = () => {
        const main = document.getElementById('main');
        const input = document.getElementById('SearchBar_Input') as HTMLInputElement;

        if (!main || !input) return;

        const inputValue = input.value;
        const genreString = selectedGenres.join(',');
        
        console.log('Выбранные жанры:', genreString);
        console.log('Введенный текст:', inputValue);

        ReactDOM.createRoot(main).render(
            <CatalogPage 
                keyWords={ inputValue }
                genres={ genreString }
            />
        );
    };

    const toggleGenre = (genre: string) => {
        setSelectedGenres((prevGenres) => {
            if (prevGenres.includes(genre)) {
                return prevGenres.filter((g) => g !== genre);
            }
            return [...prevGenres, genre];
        });
    };

    return (
        <div className="SearchBar">
            <div className="SearchBar_Inputs">
                <input id="SearchBar_Input" className="SearchBar_InputField" placeholder="Введите запрос" />
                <button className="SearchBar_Button" onClick={handleSearchClicked}>
                    <IconSearch className="SearchBar_Button_Icon" />
                    <p className="SearchBar_Button_Text">Найти</p>
                </button>
                <button
                    className="SearchBar_Button Filters_Button"
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                >
                    Фильтры
                </button>
            </div>
            <div className="SearchBar_Filters">
                {
                    isMenuOpen && (
                        <div className="Filters_Menu">
                            <p>Выберите жанры:</p>
                                {
                                    genreOptions.map((genre) => (
                                        <label key={genre.id} className="Filter_Option">
                                            <input
                                                type="checkbox"
                                                checked={selectedGenres.includes(genre.name)}
                                                onChange={() => toggleGenre(genre.name)}
                                            />
                                            {genre.name}
                                        </label>
                                    ))
                                }
                        </div>
                    )
                }
            </div>
        </div>
    );
}
