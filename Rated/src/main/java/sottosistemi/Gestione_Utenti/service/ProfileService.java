package sottosistemi.Gestione_Utenti.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import model.DAO.UtenteDAO;
import model.DAO.PreferenzaDAO;
import model.DAO.InteresseDAO;
import model.DAO.VistoDAO;
import model.Entity.UtenteBean;
import model.Entity.VistoBean;
import utilities.PasswordUtility;
import model.Entity.FilmBean;
import model.Entity.InteresseBean;
import model.Entity.PreferenzaBean;
import model.Entity.RecensioneBean;

public class ProfileService {
    
    public final UtenteDAO UtenteDAO;
    public final PreferenzaDAO PreferenzaDAO;
    public final InteresseDAO InteresseDAO;
    public final VistoDAO VistoDAO;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant UtenteDAO != null;
    //@ public invariant PreferenzaDAO != null;
    //@ public invariant InteresseDAO != null;
    //@ public invariant VistoDAO != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.UtenteDAO != null;
    //@ ensures this.PreferenzaDAO != null;
    //@ ensures this.InteresseDAO != null;
    //@ ensures this.VistoDAO != null;
    public ProfileService() {
        this.UtenteDAO = new UtenteDAO();
        this.PreferenzaDAO = new PreferenzaDAO();
        this.InteresseDAO = new InteresseDAO();
        this.VistoDAO = new VistoDAO();
    }
    
    //@ requires dataSource != null;
    //@ ensures this.UtenteDAO != null;
    //@ ensures this.PreferenzaDAO != null;
    //@ ensures this.InteresseDAO != null;
    //@ ensures this.VistoDAO != null;
    public ProfileService(final DataSource dataSource) { 
        this.UtenteDAO = new UtenteDAO(dataSource);
        this.PreferenzaDAO = new PreferenzaDAO(dataSource);
        this.InteresseDAO = new InteresseDAO(dataSource);
        this.VistoDAO = new VistoDAO(dataSource);
    }

    //@ requires utenteDAO != null;
    //@ requires PreferenzaDAO != null;
    //@ requires InteresseDAO != null;
    //@ requires VistoDAO != null;
    //@ ensures this.UtenteDAO == utenteDAO;
    //@ ensures this.PreferenzaDAO == PreferenzaDAO;
    //@ ensures this.InteresseDAO == InteresseDAO;
    //@ ensures this.VistoDAO == VistoDAO;
    public ProfileService(final UtenteDAO utenteDAO, final PreferenzaDAO PreferenzaDAO, final InteresseDAO InteresseDAO, final VistoDAO VistoDAO) { 
        this.UtenteDAO = utenteDAO;
        this.PreferenzaDAO = PreferenzaDAO;
        this.InteresseDAO = InteresseDAO;
        this.VistoDAO = VistoDAO;
    }
    
    /* =========================================
     * METODI SERVICE
     * ========================================= */

    /*@ 
      @ requires username != null;
      @ requires email != null;
      @ requires password != null;
      @ assignable \everything;
      @ skiprac
      @*/
    public UtenteBean ProfileUpdate(final String username, final String email, final String password, final String biografia, final byte[] icon) { 
        
        final UtenteBean u = UtenteDAO.findByUsername(username); 
        if(u != null && !(u.getEmail().equals(email)))
            return null;
        
        final UtenteBean user = UtenteDAO.findByEmail(email); 
        if (user != null) {
            user.setUsername(username);
            
            final String rawHash = PasswordUtility.hashPassword(password);
            final String hash = (rawHash == null) ? "" : rawHash; // Risolto: ora hash è final
            user.setPassword(hash);
            
            user.setBiografia(biografia);
            user.setIcona(icon);
            UtenteDAO.update(user);
        }
        
        return user;
    }
    
    /*@ 
      @ requires email != null;
      @ requires password != null;
      @ assignable \everything;
      @ skiprac
      @*/
    public UtenteBean PasswordUpdate(final String email, final String password) { 
        
        final UtenteBean user = UtenteDAO.findByEmail(email); 
        if(user == null)
            return null;
        
        final String rawHash = PasswordUtility.hashPassword(password);
        final String hash = (rawHash == null) ? "" : rawHash; // Risolto: ora hash è final
        user.setPassword(hash);
        
        UtenteDAO.update(user);
        
        return user;
    }
    
    //@ requires username != null;
    //@ assignable \everything;
    public UtenteBean findByUsername(final String username) { 
        return UtenteDAO.findByUsername(username);
    }
    
    /*@ 
      @ requires recensioni != null;
      @ assignable \everything;
      @ ensures \result != null;
      @ skiprac
      @*/
    public HashMap<String, String> getUsers(final List<RecensioneBean> recensioni) { 
        final HashMap<String, String> users = new HashMap<String, String>(); 
        
        for(int i = 0; i < recensioni.size(); i++) { 
            final RecensioneBean recensione = recensioni.get(i); // Risolto: final
            if (recensione != null) {
                final String em = recensione.getEmail(); 
                final UtenteBean u = UtenteDAO.findByEmail(em);
                if (u != null) {
                    final String un = u.getUsername(); 
                    users.put(em, un);
                }
            }
        }
        return users;
    }
    
    /*@ 
      @ requires email != null;
      @ assignable \everything;
      @ ensures \result != null;
      @ skiprac
      @*/
    public List<String> getPreferenze(final String email){
        final List<PreferenzaBean> preferenze = PreferenzaDAO.findByEmail(email); // Risolto: final
        final List<String> preferenzeString = new ArrayList<String>(); // Risolto: final
        
        if (preferenze != null) {
            for(int i = 0; i < preferenze.size(); i++) { 
                final PreferenzaBean b = preferenze.get(i); // Risolto: final
                if (b != null) {
                    preferenzeString.add(b.getNomeGenere());
                }
            }
        }
        return preferenzeString;
    }
    
    //@ requires email != null;
    //@ requires genere != null;
    //@ assignable \everything;
    public void addPreferenza(final String email, final String genere) {
        final PreferenzaBean preferenza = new PreferenzaBean(email, genere); // Risolto: final
        PreferenzaDAO.save(preferenza);
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void aggiungiAllaWatchlist(final String email, final int filmId){ // Parametri final
        final InteresseBean interesse = new InteresseBean(); // Risolto: final
        interesse.setEmail(email);
        interesse.setIdFilm(filmId);
        interesse.setInteresse(true);
        InteresseDAO.save(interesse);
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void aggiungiFilmVisto(final String email, final int filmId){ // Parametri final
        final VistoBean visto = new VistoBean(); // Risolto: final
        visto.setEmail(email);
        visto.setIdFilm(filmId);
        VistoDAO.save(visto);
    }
    
    /*@ 
      @ requires email != null;
      @ assignable \everything;
      @ skiprac
      @*/
    public void aggiornaPreferenzeUtente(final String email, final String[] idGeneri){ // Parametri final
        PreferenzaDAO.deleteByEmail(email);
            
        if (idGeneri != null && idGeneri.length > 0) {
            for (int i = 0; i < idGeneri.length; i++) {
                final String idGenereStr = idGeneri[i]; // Risolto: final
                if (idGenereStr != null) {
                    final PreferenzaBean preferenza = new PreferenzaBean(); // Risolto: final
                    preferenza.setEmail(email);
                    preferenza.setNomeGenere(idGenereStr);
                    PreferenzaDAO.save(preferenza);
                }
            }
        }
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void ignoreFilm(final String email, final int filmId){ // Parametri final
        final InteresseBean interesse = new InteresseBean(); // Risolto: final
        interesse.setEmail(email);
        interesse.setIdFilm(filmId);
        interesse.setInteresse(false);
        InteresseDAO.save(interesse);
    }
    
    //@ requires username != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> retrieveWatchedFilms(final String username) {
        final List<FilmBean> res = VistoDAO.doRetrieveFilmsByUtente(username); // Risolto: final
        return res != null ? res : new ArrayList<FilmBean>();
    }

    //@ requires username1 != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> retrieveWatchlist(final String username1) {
        final List<FilmBean> res = this.InteresseDAO.doRetrieveFilmsByUtente(username1); // Risolto: final
        return res != null ? res : new ArrayList<FilmBean>();
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public boolean isFilmInWatchlist(final String email, final int filmId) { // Parametri final
            final InteresseBean interesseBean = this.InteresseDAO.findByEmailAndIdFilm(email, filmId); // Risolto: final
            if (interesseBean == null) {
                return false;
            }
            return interesseBean.isInteresse();
    }

    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void rimuoviDallaWatchlist(final String email, final int filmId) { // Parametri final
        this.InteresseDAO.delete(email, filmId);
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public boolean isFilmVisto(final String email, final int filmId) { // Parametri final
        return this.VistoDAO.findByEmailAndIdFilm(email, filmId) != null;
    }

    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void rimuoviFilmVisto(final String email, final int filmId) { // Parametri final
        this.VistoDAO.delete(email, filmId);
    }
}