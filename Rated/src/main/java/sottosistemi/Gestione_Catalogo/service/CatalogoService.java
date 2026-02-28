package sottosistemi.Gestione_Catalogo.service;

import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class CatalogoService {

    //@ spec_public
    private final FilmDAO FilmDAO; 
    //@ spec_public
    private final FilmGenereDAO FilmGenereDAO;
    //@ spec_public
    private final GenereDAO GenereDAO;
    
    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant FilmDAO != null;
    //@ public invariant FilmGenereDAO != null;
    //@ public invariant GenereDAO != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.FilmDAO != null;
    //@ ensures this.FilmGenereDAO != null;
    //@ ensures this.GenereDAO != null;
    public CatalogoService() {
        this.FilmDAO = new FilmDAO();
        this.FilmGenereDAO = new FilmGenereDAO();
        this.GenereDAO = new GenereDAO();
    }
    
    // Costruttore per il test
    //@ requires filmDAO != null;
    //@ requires FilmGenereDAO != null;
    //@ requires GenereDAO != null;
    //@ ensures this.FilmDAO == filmDAO;
    //@ ensures this.FilmGenereDAO == FilmGenereDAO;
    //@ ensures this.GenereDAO == GenereDAO;
    public CatalogoService(final FilmDAO filmDAO, final FilmGenereDAO FilmGenereDAO, final GenereDAO GenereDAO) {
        this.FilmDAO = filmDAO;
        this.FilmGenereDAO = FilmGenereDAO;
        this.GenereDAO = GenereDAO;
    }

    /* =========================================
     * METODI SERVICE
     * ========================================= */
    
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> getFilms(){
        final List<FilmBean> films = FilmDAO.findAll(); // Risolto: final
        return films;
    }

    //@ requires nome != null;
    //@ requires anno >= 0;
    //@ requires durata >= 0;
    //@ requires generi != null;
    //@ requires regista != null;
    //@ requires attori != null;
    //@ requires trama != null;
    //@ assignable \everything;
    public void aggiungiFilm(final String nome, final int anno, final int durata, final String[] generi, final String regista, final String attori, final byte[] locandina, final String trama) {
        final FilmBean film = new FilmBean(); // Risolto: final
        film.setNome(nome);
        film.setAnno(anno);
        film.setDurata(durata);
        film.setRegista(regista);
        film.setAttori(attori);
        film.setLocandina(locandina);
        film.setTrama(trama);
        FilmDAO.save(film);
    }

    //@ requires film != null;
    //@ assignable \everything;
    public void removeFilmByBean(final FilmBean film) {
        FilmDAO.delete(film.getIdFilm());
    }

    //@ requires name != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> ricercaFilm(final String name) {
        return FilmDAO.findByName(name);
    }

    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public FilmBean getFilm(final int idFilm) {
        return FilmDAO.findById(idFilm);
    }
    
    //@ requires recensioni != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public HashMap<Integer, FilmBean> getFilms(final List<RecensioneBean> recensioni) {
        final HashMap<Integer, FilmBean> FilmMap = new HashMap<>(); // Risolto: final
        for(final RecensioneBean Recensione : recensioni) {
            if (Recensione != null) {
                final int key = Recensione.getIdFilm(); // Risolto: final
                final FilmBean Film = this.getFilm(key); // Risolto: final
                FilmMap.put(key, Film);
            }
        }
        return FilmMap;
    }
    
    //@ requires anno >= 0;
    //@ requires Attori != null;
    //@ requires durata >= 0;
    //@ requires Generi != null;
    //@ requires Nome != null;
    //@ requires Regista != null;
    //@ requires Trama != null;
    //@ assignable \everything;
    public void addFilm(final int anno, final String Attori, final int durata, final String[] Generi, final byte[] Locandina, final String Nome, final String Regista, final String Trama){
        // Qui 'film' non può essere final perché viene riassegnato alla riga successiva dopo la ricerca
        FilmBean film = new FilmBean();
        film.setAnno(anno);
        film.setAttori(Attori);
        film.setDurata(durata);
        film.setLocandina(Locandina);
        film.setNome(Nome);
        film.setRegista(Regista);
        film.setTrama(Trama);
        FilmDAO.save(film);
        
        final List<FilmBean> films = FilmDAO.findByName(Nome); // Risolto: final
        if (films != null && !films.isEmpty()) {
            film = films.get(0); // Riassegnazione (corretto che non sia final)
            for(final String genere : Generi){ // Risolto: final nel loop
                if (genere != null) {
                    final FilmGenereBean FilmGenere = new FilmGenereBean(film.getIdFilm(), genere); // Risolto: final
                    FilmGenereDAO.save(FilmGenere);
                }
            }
        }
    }
    
    //@ requires idFilm >= 0;
    //@ requires anno >= 0;
    //@ requires Attori != null;
    //@ requires durata >= 0;
    //@ requires Generi != null;
    //@ requires Nome != null;
    //@ requires Regista != null;
    //@ requires Trama != null;
    //@ assignable \everything;
    public void modifyFilm(final int idFilm, final int anno, final String Attori, final int durata, final String[] Generi, final byte[] Locandina, final String Nome, final String Regista, final String Trama){
        final FilmBean film = new FilmBean(); // Risolto: final
        film.setIdFilm(idFilm);
        film.setAnno(anno);
        film.setAttori(Attori);
        film.setDurata(durata);
        film.setLocandina(Locandina);
        film.setNome(Nome);
        film.setRegista(Regista);
        film.setTrama(Trama);
        
        final FilmBean filmAttuale = FilmDAO.findById(idFilm); // Risolto: final
        if (filmAttuale != null) {
            film.setValutazione(filmAttuale.getValutazione());
        } else {
            film.setValutazione(1); 
        }
        FilmDAO.update(film);
        
        FilmGenereDAO.deleteByIdFilm(idFilm);
        for(final String genere : Generi){ // Risolto: final nel loop
            if (genere != null) {
                final FilmGenereBean FilmGenere = new FilmGenereBean(film.getIdFilm(), genere); // Risolto: final
                FilmGenereDAO.save(FilmGenere);
            }
        }
    }
    
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public void removeFilm(final int idFilm) {
        FilmDAO.delete(idFilm);
    }
    
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmGenereBean> getGeneri(final int idFilm) {
        return FilmGenereDAO.findByIdFilm(idFilm);
    }
    
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<String> getAllGeneri(){
        return GenereDAO.findAllString();
    }
    
    //@ requires utente != null;
    //@ requires utente.getEmail() != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> getFilmCompatibili(final UtenteBean utente) { // Risolto: parametro final
        return this.FilmDAO.doRetrieveConsigliati(utente.getEmail());
    }
}