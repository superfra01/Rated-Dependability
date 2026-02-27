package sottosistemi.Gestione_Recensioni.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.FilmDAO;
import model.Entity.RecensioneBean;
import model.Entity.ReportBean;
import model.Entity.ValutazioneBean;
import model.Entity.FilmBean;
import model.DAO.ValutazioneDAO;

public class RecensioniService {

    public final RecensioneDAO RecensioneDAO; 
    public final ValutazioneDAO ValutazioneDAO; 
    public final ReportDAO ReportDAO; 
    public final FilmDAO FilmDAO; 

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant RecensioneDAO != null;
    //@ public invariant ValutazioneDAO != null;
    //@ public invariant ReportDAO != null;
    //@ public invariant FilmDAO != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.RecensioneDAO != null;
    //@ ensures this.ValutazioneDAO != null;
    //@ ensures this.ReportDAO != null;
    //@ ensures this.FilmDAO != null;
    public RecensioniService() {
        this.RecensioneDAO = new RecensioneDAO();
        this.ValutazioneDAO = new ValutazioneDAO();
        this.ReportDAO = new ReportDAO();
        this.FilmDAO = new FilmDAO();
    }
    
    // Costruttore personalizzato per i test
    //@ requires recensioneDAO != null;
    //@ requires valutazioneDAO != null;
    //@ requires reportDAO != null;
    //@ requires filmDAO != null;
    //@ ensures this.RecensioneDAO == recensioneDAO;
    //@ ensures this.ValutazioneDAO == valutazioneDAO;
    //@ ensures this.ReportDAO == reportDAO;
    //@ ensures this.FilmDAO == filmDAO;
    public RecensioniService(final RecensioneDAO recensioneDAO, final ValutazioneDAO valutazioneDAO, final ReportDAO reportDAO, final FilmDAO filmDAO) { // Parametri final
        this.RecensioneDAO = recensioneDAO;
        this.ValutazioneDAO = valutazioneDAO;
        this.ReportDAO = reportDAO;
        this.FilmDAO = filmDAO;
    }
    
    /* =========================================
     * METODI SERVICE
     * ========================================= */

    //@ requires email != null;
    //@ requires email_recensore != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public synchronized void addValutazione(final String email, final int idFilm, final String email_recensore, final boolean nuovaValutazione) { // Parametri final
        // Recupero la valutazione esistente, se presente
        final ValutazioneBean valutazioneEsistente = ValutazioneDAO.findById(email, email_recensore, idFilm); // Locale final
        final RecensioneBean recensione = RecensioneDAO.findById(email_recensore, idFilm); // Locale final

        if (recensione == null) {
            throw new IllegalArgumentException("Recensione non trovata.");
        }

        // Gestione dei contatori in base alla valutazione corrente
        if (valutazioneEsistente != null) {
            final boolean valutazioneCorrente = valutazioneEsistente.isLikeDislike(); // Locale final

            // Caso 1: L'utente ha cambiato la valutazione
            if (valutazioneCorrente != nuovaValutazione) {
                if (nuovaValutazione) {
                    recensione.setNLike(recensione.getNLike() + 1);
                    recensione.setNDislike(recensione.getNDislike() - 1);
                } else {
                    recensione.setNLike(recensione.getNLike() - 1);
                    recensione.setNDislike(recensione.getNDislike() + 1);
                }
                valutazioneEsistente.setLikeDislike(nuovaValutazione);
                ValutazioneDAO.save(valutazioneEsistente);
            }
            // Caso 2: L'utente rimuove la valutazione
            else {
                if (valutazioneCorrente) {
                    recensione.setNLike(recensione.getNLike() - 1);
                } else {
                    recensione.setNDislike(recensione.getNDislike() - 1);
                }
                ValutazioneDAO.delete(email, email_recensore, idFilm);
            }
        }
        // Caso 3: Nuova valutazione
        else {
            final ValutazioneBean nuovaValutazioneBean = new ValutazioneBean(); // Locale final
            nuovaValutazioneBean.setEmail(email);
            nuovaValutazioneBean.setEmailRecensore(email_recensore);
            nuovaValutazioneBean.setIdFilm(idFilm);
            nuovaValutazioneBean.setLikeDislike(nuovaValutazione);
            ValutazioneDAO.save(nuovaValutazioneBean);

            if (nuovaValutazione) {
                recensione.setNLike(recensione.getNLike() + 1);
            } else {
                recensione.setNDislike(recensione.getNDislike() + 1);
            }
        }

        // Aggiornamento della recensione nel database
        RecensioneDAO.update(recensione);
    }
    
    //@ requires email != null;
    //@ requires recensione != null;
    //@ requires titolo != null;
    //@ requires idFilm >= 0;
    //@ requires valutazione >= 0;
    //@ assignable \everything;
    public synchronized void addRecensione(final String email, final int idFilm, final String recensione, final String titolo, final int valutazione) { // Parametri final
        if (RecensioneDAO.findById(email, idFilm) != null)
            return;

        // Crea la nuova recensione
        final RecensioneBean nuovaRecensione = new RecensioneBean();
        nuovaRecensione.setEmail(email);
        nuovaRecensione.setTitolo(titolo);
        nuovaRecensione.setIdFilm(idFilm);
        nuovaRecensione.setContenuto(recensione);
        nuovaRecensione.setValutazione(valutazione);
        RecensioneDAO.save(nuovaRecensione);

        // Aggiorna la valutazione media del film
        final FilmBean film = FilmDAO.findById(idFilm);
        final List<RecensioneBean> recensioni = RecensioneDAO.findByIdFilm(idFilm);

        if (film != null) {
            if (recensioni != null && recensioni.isEmpty()) {
                film.setValutazione(0); // Nessuna recensione: valutazione predefinita
            } else if (recensioni != null) {
                int somma = 0;
                for (final RecensioneBean recensioneFilm : recensioni) {
                    if (recensioneFilm != null) {
                        somma += recensioneFilm.getValutazione();
                    }
                }
                final int media = somma / recensioni.size();
                film.setValutazione(media);
            }
            FilmDAO.update(film);
        }
    }

    //@ requires email != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<RecensioneBean> FindRecensioni(final String email) { // Parametro final
        final List<RecensioneBean> recensioni = RecensioneDAO.findByUser(email);
        return recensioni != null ? recensioni : new ArrayList<RecensioneBean>();
    }
    
    //@ requires email != null;
    //@ requires ID_Film >= 0;
    //@ assignable \everything;
    public synchronized void deleteRecensione(final String email, final int ID_Film) { // Parametri final
        // Prima elimina i report associati
        ReportDAO.deleteReports(email, ID_Film);

        // Poi elimina le valutazioni
        ValutazioneDAO.deleteValutazioni(email, ID_Film);

        // Infine elimina la recensione
        RecensioneDAO.delete(email, ID_Film);

        // Recupera il film e aggiorna la valutazione media
        final FilmBean film = FilmDAO.findById(ID_Film);
        final List<RecensioneBean> recensioni = RecensioneDAO.findByIdFilm(ID_Film);

        if (film != null) {
            if (recensioni != null && recensioni.isEmpty()) {
                // Nessuna recensione rimasta: impostare valutazione al valore minimo consentito
                film.setValutazione(1); // Il valore 1 è il minimo consentito dal CHECK constraint
            } else if (recensioni != null) {
                int somma = 0;
                for (final RecensioneBean recensionefilm : recensioni) {
                    if (recensionefilm != null) {
                        somma += recensionefilm.getValutazione();
                    }
                }
                final int media = somma / recensioni.size();
                film.setValutazione(media);
            }
            // Aggiorna il film con la nuova valutazione
            FilmDAO.update(film);
        }
    }

    //@ requires email != null;
    //@ requires ID_Film >= 0;
    //@ assignable \everything;
    public void deleteReports(final String email, final int ID_Film) { // Parametri final
        final RecensioneBean recensione = RecensioneDAO.findById(email, ID_Film);
        if (recensione != null) {
            recensione.setNReports(0);
            RecensioneDAO.update(recensione);
        }
        
        ReportDAO.deleteReports(email, ID_Film);
    }
    
    //@ requires ID_film >= 0;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<RecensioneBean> GetRecensioni(final int ID_film){ // Parametro final
        List<RecensioneBean> result = RecensioneDAO.findByIdFilm(ID_film);
        return result != null ? result : new ArrayList<RecensioneBean>();
    }
    
    //@ requires ID_film >= 0;
    //@ requires email != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public HashMap<String, ValutazioneBean> GetValutazioni(final int ID_film, final String email){ // Parametri final
        HashMap<String, ValutazioneBean> result = ValutazioneDAO.findByIdFilmAndEmail(ID_film, email);
        return result != null ? result : new HashMap<String, ValutazioneBean>();
    }
    
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<RecensioneBean> GetAllRecensioniSegnalate(){
        final List<RecensioneBean> recensioni = RecensioneDAO.findAll();
        final List<RecensioneBean> recensioniFiltered = new ArrayList<RecensioneBean>();
        if (recensioni != null) {
            for(final RecensioneBean recensione : recensioni) {
                if(recensione != null && recensione.getNReports()!=0) {
                    recensioniFiltered.add(recensione);
                }
            }
        }
        
        return recensioniFiltered;
    }
    
    //@ requires email != null;
    //@ requires emailRecensore != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public synchronized void report(final String email, final String emailRecensore, final int idFilm) { // Parametri final
        
        final ReportBean report = new ReportBean();
        report.setEmailRecensore(emailRecensore);
        report.setEmail(email);
        report.setIdFilm(idFilm);
        if(ReportDAO.findById(email, emailRecensore, idFilm)==null) {
            
            final RecensioneBean recensione = RecensioneDAO.findById(emailRecensore, idFilm);
            if (recensione != null) {
                recensione.setNReports(recensione.getNReports()+1);
                RecensioneDAO.update(recensione);
            }
            ReportDAO.save(report);
        }
    }
    
    //@ requires email != null;
    //@ requires filmId >= 0;
    //@ assignable \everything;
    public RecensioneBean getRecensione(int filmId, String email) {
        // NB: in questo specifico metodo stai istanziando un nuovo DAO locale
        model.DAO.RecensioneDAO recensioneDAO = new model.DAO.RecensioneDAO();
        return recensioneDAO.findById(email, filmId);
    }
}