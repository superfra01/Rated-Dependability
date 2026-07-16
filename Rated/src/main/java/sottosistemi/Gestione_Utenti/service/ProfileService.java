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
    //@ public invariant UtenteDAO.dataSource != null;
    //@ public invariant PreferenzaDAO != null;
    //@ public invariant PreferenzaDAO.dataSource != null;
    //@ public invariant InteresseDAO != null;
    //@ public invariant VistoDAO != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.UtenteDAO != null;
    //@ ensures this.PreferenzaDAO != null;
    //@ ensures this.InteresseDAO != null;
    //@ ensures this.VistoDAO != null;
    //@ assignable \nothing;
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
    //@ assignable \nothing;
    public ProfileService(final DataSource dataSource) { 
        this.UtenteDAO = new UtenteDAO(dataSource);
        this.PreferenzaDAO = new PreferenzaDAO(dataSource);
        this.InteresseDAO = new InteresseDAO(dataSource);
        this.VistoDAO = new VistoDAO(dataSource);
    }

    //@ requires utenteDAO != null;
    //@ requires utenteDAO.dataSource != null;
    //@ requires PreferenzaDAO != null;
    //@ requires PreferenzaDAO.dataSource != null;
    //@ requires InteresseDAO != null;
    //@ requires VistoDAO != null;
    //@ ensures this.UtenteDAO == utenteDAO;
    //@ ensures this.PreferenzaDAO == PreferenzaDAO;
    //@ ensures this.InteresseDAO == InteresseDAO;
    //@ ensures this.VistoDAO == VistoDAO;
    //@ assignable \nothing;
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
      @ requires biografia != null;
      @ assignable \everything;
      @ ensures \result != null ==> \result.getEmail().equals(email) && \result.getUsername().equals(username);
      @ ensures \result != null ==> \result.getPassword().length() == 56;
      @ ensures \result != null ==> \result.getBiografia().equals(biografia) && \result.getIcona() == icon;
      @*/
    public /*@ nullable @*/ UtenteBean ProfileUpdate(final String username, final String email, final String password, final String biografia, final byte /*@ nullable @*/ [] icon) {
        
        final /*@ nullable @*/ UtenteBean u = UtenteDAO.findByUsername(username);
        if(u != null && !(u.getEmail().equals(email)))
            return null;
        
        final /*@ nullable @*/ UtenteBean user = UtenteDAO.findByEmail(email);
        if (user != null) {
            user.setUsername(username);
            
            // Il contratto di PasswordUtility garantisce un risultato non nullo.
            final String hash = PasswordUtility.hashPassword(password);
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
      @ ensures \result != null ==> \result.getEmail().equals(email);
      @ ensures \result != null ==> \result.getPassword().length() == 56;
      @*/
    public /*@ nullable @*/ UtenteBean PasswordUpdate(final String email, final String password) {
        
        final /*@ nullable @*/ UtenteBean user = UtenteDAO.findByEmail(email);
        if(user == null)
            return null;
        
        // Il contratto di PasswordUtility garantisce un risultato non nullo.
        final String hash = PasswordUtility.hashPassword(password);
        user.setPassword(hash);
        
        UtenteDAO.update(user);
        
        return user;
    }
    
    //@ requires username != null;
    //@ assignable \nothing;
    //@ ensures \result != null ==> \result.getUsername().equals(username);
    public /*@ nullable @*/ UtenteBean findByUsername(final String username) {
        return UtenteDAO.findByUsername(username);
    }
    
    /*@ 
      @ requires recensioni != null;
      @ requires 0 <= recensioni.size();
      @ requires (\forall int i; 0 <= i && i < recensioni.size(); recensioni.get(i) != null);
      @ assignable \nothing;
      @ ensures \result != null;
      @*/
    public HashMap<String, String> getUsers(final List<RecensioneBean> recensioni) { 
        final HashMap<String, String> users = new HashMap<String, String>(); 
        
        final int size = recensioni.size();
        /*@ loop_invariant 0 <= i && i <= size;
          @ loop_invariant size == recensioni.size();
          @ decreases size - i;
          @*/
        for(int i = 0; i < size; ++i) { 
            final RecensioneBean recensione = recensioni.get(i); // Risolto: final
            if (recensione != null) {
                final String em = recensione.getEmail(); 
                final /*@ nullable @*/ UtenteBean u = UtenteDAO.findByEmail(em);
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
      @ assignable \nothing;
      @ ensures \result != null;
      @ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null);
      @*/
    public List<String> getPreferenze(final String email){
        final List<PreferenzaBean> preferenze = PreferenzaDAO.findByEmail(email); // Risolto: final
        final List<String> preferenzeString = new ArrayList<String>(); // Risolto: final
        
        if (preferenze != null) {
        	final int size = preferenze.size();
            /*@ loop_invariant 0 <= i && i <= size;
              @ loop_invariant size == preferenze.size();
              @ loop_invariant (\forall int j; 0 <= j && j < preferenzeString.size(); preferenzeString.get(j) != null);
              @ decreases size - i;
              @*/
            for(int i = 0; i < size; ++i) { 
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
      @*/
    public void aggiornaPreferenzeUtente(final String email, final /*@ nullable @*/ String /*@ nullable @*/ [] idGeneri){ // Parametri final
        PreferenzaDAO.deleteByEmail(email);
            
        if (idGeneri != null && idGeneri.length > 0) {
            /*@ loop_invariant 0 <= i && i <= idGeneri.length;
              @ decreases idGeneri.length - i;
              @*/
            for (int i = 0; i < idGeneri.length; ++i) {
                final /*@ nullable @*/ String idGenereStr = idGeneri[i]; // Risolto: final
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
    //@ assignable \nothing;
    //@ ensures \result != null;
    public List<FilmBean> retrieveWatchedFilms(final String username) {
        final List<FilmBean> res = VistoDAO.doRetrieveFilmsByUtente(username); // Risolto: final
        return res != null ? res : new ArrayList<FilmBean>();
    }

    //@ requires username1 != null;
    //@ assignable \nothing;
    //@ ensures \result != null;
    public List<FilmBean> retrieveWatchlist(final String username1) {
        final List<FilmBean> res = this.InteresseDAO.doRetrieveFilmsByUtente(username1); // Risolto: final
        return res != null ? res : new ArrayList<FilmBean>();
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \nothing;
    public boolean isFilmInWatchlist(final String email, final int filmId) { // Parametri final
            final /*@ nullable @*/ InteresseBean interesseBean = this.InteresseDAO.findByEmailAndIdFilm(email, filmId); // Risolto: final
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
    //@ assignable \nothing;
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
