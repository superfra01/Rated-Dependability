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
            
            String hash = PasswordUtility.hashPassword(password);
            if (hash == null) hash = "";
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
        
        String hash = PasswordUtility.hashPassword(password);
        if (hash == null) hash = "";
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
            RecensioneBean recensione = recensioni.get(i);
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
        List<PreferenzaBean> preferenze = PreferenzaDAO.findByEmail(email);
        List<String> preferenzeString = new ArrayList<String>();
        
        if (preferenze != null) {
            for(int i = 0; i < preferenze.size(); i++) {
                PreferenzaBean b = preferenze.get(i);
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
        PreferenzaBean preferenza = new PreferenzaBean(email, genere);
        PreferenzaDAO.save(preferenza);
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void aggiungiAllaWatchlist(String email, int filmId){
        InteresseBean interesse = new InteresseBean();
        interesse.setEmail(email);
        interesse.setIdFilm(filmId);
        interesse.setInteresse(true);
        InteresseDAO.save(interesse);
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void aggiungiFilmVisto(String email, int filmId){
        VistoBean visto = new VistoBean();
        visto.setEmail(email);
        visto.setIdFilm(filmId);
        VistoDAO.save(visto);
    }
    
    /*@ 
      @ requires email != null;
      @ assignable \everything;
      @ skiprac
      @*/
    public void aggiornaPreferenzeUtente(String email, String[] idGeneri){
        PreferenzaDAO.deleteByEmail(email);
            
        if (idGeneri != null && idGeneri.length > 0) {
            for (int i = 0; i < idGeneri.length; i++) {
                String idGenereStr = idGeneri[i];
                if (idGenereStr != null) {
                    PreferenzaBean preferenza = new PreferenzaBean();
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
    public void ignoreFilm(String email, int filmId){
        InteresseBean interesse = new InteresseBean();
        interesse.setEmail(email);
        interesse.setIdFilm(filmId);
        interesse.setInteresse(false);
        InteresseDAO.save(interesse);
    }
    
    //@ requires username != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> retrieveWatchedFilms(final String username) {
        List<FilmBean> res = VistoDAO.doRetrieveFilmsByUtente(username);
        return res != null ? res : new ArrayList<FilmBean>();
    }

    //@ requires username1 != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> retrieveWatchlist(final String username1) {
        List<FilmBean> res = this.InteresseDAO.doRetrieveFilmsByUtente(username1);
        return res != null ? res : new ArrayList<FilmBean>();
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public boolean isFilmInWatchlist(String email, int filmId) {
            InteresseBean interesseBean = this.InteresseDAO.findByEmailAndIdFilm(email, filmId);
            if (interesseBean == null) {
                return false;
            }
            return interesseBean.isInteresse();
    }

    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void rimuoviDallaWatchlist(String email, int filmId) {
        this.InteresseDAO.delete(email, filmId);
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public boolean isFilmVisto(String email, int filmId) {
        return this.VistoDAO.findByEmailAndIdFilm(email, filmId) != null;
    }

    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public void rimuoviFilmVisto(String email, int filmId) {
        this.VistoDAO.delete(email, filmId);
    }
}