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
import java.util.jar.Attributes.Name;

public class CatalogoService {
    private final FilmDAO FilmDAO; // Reso final
    private final FilmGenereDAO FilmGenereDAO;
    private final GenereDAO GenereDAO;
    
    public CatalogoService() {
        this.FilmDAO = new FilmDAO();
        this.FilmGenereDAO = new FilmGenereDAO();
        this.GenereDAO = new GenereDAO();
    }
    
    // Costruttore per il test o configurazioni personalizzate
    public CatalogoService(final FilmDAO filmDAO, final FilmGenereDAO FilmGenereDAO, final GenereDAO GenereDAO) { // Parametro final
        this.FilmDAO = filmDAO;
        this.FilmGenereDAO = FilmGenereDAO;
        this.GenereDAO = GenereDAO;
    }
    
    public List<FilmBean> getFilms(){
    	final List<FilmBean> films = FilmDAO.findAll(); // Variabile locale final
    	return films;
    }

    public void aggiungiFilm(final String nome, final int anno, final int durata, final String[] generi, final String regista, final String attori, final byte[] locandina, final String trama) { // Parametri final
        final FilmBean film = new FilmBean();
        film.setNome(nome);
        film.setAnno(anno);
        film.setDurata(durata);
        
        film.setRegista(regista);
        film.setAttori(attori);
        film.setLocandina(locandina);
        film.setTrama(trama);
        FilmDAO.save(film);
        
    }

    public void removeFilmByBean(final FilmBean film) { // Parametro final
        FilmDAO.delete(film.getIdFilm());
    }

    public List<FilmBean> ricercaFilm(final String name) { // Parametro final
        return FilmDAO.findByName(name);
    }

    public FilmBean getFilm(final int idFilm) { // Parametro final
        return FilmDAO.findById(idFilm);
    }
    
    public HashMap<Integer, FilmBean> getFilms(final List<RecensioneBean> recensioni) { // Parametro final
    	
    	final HashMap<Integer, FilmBean> FilmMap = new HashMap<>(); // Variabile locale final
    	for(final RecensioneBean Recensione : recensioni) {
    		final int key = Recensione.getIdFilm();
    		final FilmBean Film = this.getFilm(key);
    		FilmMap.put(key, Film);
    	}
        return FilmMap;
    }
    
    public void addFilm(final int anno, final String Attori, final int durata, final String[] Generi, final byte[] Locandina, final String Nome, final String Regista, final String Trama){ // Parametri final
    	FilmBean film = new FilmBean();
    	film.setAnno(anno);
    	film.setAttori(Attori);
    	film.setDurata(durata);
    	film.setLocandina(Locandina);
    	film.setNome(Nome);
    	film.setRegista(Regista);
    	film.setTrama(Trama);
    	FilmDAO.save(film);
        List<FilmBean> films = FilmDAO.findByName(Nome);
        film = films.get(0);
        for(String genere : Generi){
            FilmGenereBean FilmGenere = new FilmGenereBean(film.getIdFilm(), genere);
            FilmGenereDAO.save(FilmGenere);
        }
    }
    
    public void modifyFilm(final int idFilm, final int anno, final String Attori, final int durata, final String[] Generi, final byte[] Locandina, final String Nome, final String Regista, final String Trama){ // Parametri final
    	FilmBean film = new FilmBean();
    	film.setIdFilm(idFilm);
    	film.setAnno(anno);
    	film.setAttori(Attori);
    	film.setDurata(durata);
    	film.setLocandina(Locandina);
    	film.setNome(Nome);
    	film.setRegista(Regista);
    	film.setTrama(Trama);
        final FilmBean filmAttuale = FilmDAO.findById(idFilm);
        film.setValutazione(filmAttuale.getValutazione());
    	FilmDAO.update(film);
        
        FilmGenereDAO.deleteByIdFilm(idFilm);
        for(String genere : Generi){
            FilmGenereBean FilmGenere = new FilmGenereBean(film.getIdFilm(), genere);
            FilmGenereDAO.save(FilmGenere);
        }
    }
    
    public void removeFilm(final int idFilm) { // Parametro final
    	FilmDAO.delete(idFilm);
    }
    
    public List<FilmGenereBean> getGeneri(final int idFilm) {
 
    	return FilmGenereDAO.findByIdFilm(idFilm);
        
    }
    
    public List<String> getAllGeneri(){
    	return GenereDAO.findAllString();
    }
    
 // Aggiungi questo metodo in CatalogoService.java

    public List<FilmBean> getFilmCompatibili(UtenteBean utente) {
        // Il Service delega al DAO l'esecuzione della query complessa
        return this.FilmDAO.doRetrieveConsigliati(utente.getEmail());
    }
    
}