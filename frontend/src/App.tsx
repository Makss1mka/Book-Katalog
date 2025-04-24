import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import CatalogPage from './pages/CatalogPage/CatalogPage';
import "./App.css"

function App() {
    return (
        <div className='App'>
            <Header/>
            <main className='Main' id="main">
                <CatalogPage keyWords={ null } genres={ null }/>
            </main>
            <Footer/>
        </div>
    );
}

export default App;
