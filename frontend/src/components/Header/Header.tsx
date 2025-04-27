import ReactDOM from 'react-dom/client';
import { IconSearch, IconFavouriteEmpty, IconPhone, IconUser, IconBook } from "../../utils/icons"
import './Header.css';
import CatalogPage from '../../pages/CatalogPage/CatalogPage';
import FavouritePage from '../../pages/FavouritePage/FavouritePage';

export default function Header() {
    const handleCatalogClicked = () => {
        let main = document.getElementById('main');

        if (!main) return;

        ReactDOM.createRoot(main).render(
            <CatalogPage />
        );
    }

    const handleFavouriteClicked = () => {
        let main = document.getElementById('main');

        if (!main) return;

        ReactDOM.createRoot(main).render(
            <FavouritePage />
        );
    }

    return (
        <header className="Header">
            <div>
                <div className="LOGO" onClick={ handleCatalogClicked }>
                    <IconBook className='LOGO_Icon' />
                    <div className='LOGO_Texts'>
                        <div className='LOGO_Texts_Main'>Raise | Book shop</div>
                        <div className='LOGO_Texts_Sub'>Best Book shop in the universe</div>
                    </div>
                </div>
                <HeaderButton
                    className="Header_Button"
                    text="Поиск"
                    textClassName="Header_ButtonText"
                    Icon={ IconSearch }
                    iconClassName="Header_ButtonIcon"
                    lineClassName="Header_ButtinLine"
                    onClick={ handleCatalogClicked }
                />
                <HeaderButton
                    className="Header_Button"
                    text="Закладки"
                    textClassName="Header_ButtonText"
                    Icon={ IconFavouriteEmpty }
                    iconClassName="Header_ButtonIcon"
                    lineClassName="Header_ButtinLine"
                    onClick={ handleFavouriteClicked }
                />
            </div>
        </header>
    )
}

/*
            <div>
                <HeaderButton
                    className="Header_Button"
                    text="Связь с нами"
                    textClassName="Header_ButtonText"
                    Icon={ IconPhone }
                    iconClassName="Header_ButtonIcon"
                    lineClassName="Header_ButtinLine"
                    onClick={() => console.log("Button pressed")}
                />
                <HeaderButton
                    className="Header_Button"
                    text="Ваш профиль"
                    textClassName="Header_ButtonText"
                    Icon={ IconUser }
                    iconClassName="Header_ButtonIcon"
                    lineClassName="Header_ButtinLine"
                    onClick={() => console.log("Button pressed")}
                />
            </div>
*/

interface HeaderButtonProps {
    className: string;
    text: string;
    textClassName: string;
    Icon: any;
    iconClassName: string;
    lineClassName: string;
    onClick?: () => void;
}

function HeaderButton({ className, text, textClassName, Icon, iconClassName, lineClassName, onClick=()=>{} }: HeaderButtonProps) {
    return (
        <button className={className} onClick={onClick}>
            <Icon className={iconClassName}/>
            <p className={textClassName}>
                {text}
            </p>
            <div className={ lineClassName }></div>
        </button>
    )
}

