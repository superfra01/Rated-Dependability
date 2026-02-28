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
    
    //@ requires recensioneDAO != null;
    //@ requires valutazioneDAO != null;
    //@ requires reportDAO != null;
    //@ requires filmDAO != null;
    //@ ensures this.RecensioneDAO == recensioneDAO;
    //@ ensures this.ValutazioneDAO == valutazioneDAO;
    //@ ensures this.ReportDAO == reportDAO;
    //@ ensures this.FilmDAO == filmDAO;
    public RecensioniService(final RecensioneDAO recensioneDAO, final ValutazioneDAO valutazioneDAO, final ReportDAO reportDAO, final FilmDAO filmDAO) {
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
    public synchronized void addValutazione(final String email, final int idFilm, final String email_recensore, final boolean nuovaValutazione) {
        final ValutazioneBean valutazioneEsistente = ValutazioneDAO.findById(email, email_recensore, idFilm);
        final RecensioneBean recensione = RecensioneDAO.findById(email_recensore, idFilm);

        if (recensione == null) {
            throw new IllegalArgumentException("Recensione non trovata.");
        }

        if (valutazioneEsistente != null) {
            final boolean valutazioneCorrente = valutazioneEsistente.isLikeDislike();

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
            else {
                if (valutazioneCorrente) {
                    recensione.setNLike(recensione.getNLike() - 1);
                } else {
                    recensione.setNDislike(recensione.getNDislike() - 1);
                }
                ValutazioneDAO.delete(email, email_recensore, idFilm);
            }
        }
        else {
            final ValutazioneBean nuovaValutazioneBean = new ValutazioneBean();
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

        RecensioneDAO.update(recensione);
    }
    
    //@ requires email != null;
    //@ requires recensione != null;
    //@ requires titolo != null;
    //@ requires idFilm >= 0;
    //@ requires valutazione >= 0;
    //@ assignable \everything;
    public synchronized void addRecensione(final String email, final int idFilm, final String recensione, final String titolo, final int valutazione) {
        if (RecensioneDAO.findById(email, idFilm) != null)
            return;

        final RecensioneBean nuovaRecensione = new RecensioneBean(); // Risolto: final
        nuovaRecensione.setEmail(email);
        nuovaRecensione.setTitolo(titolo);
        nuovaRecensione.setIdFilm(idFilm);
        nuovaRecensione.setContenuto(recensione);
        nuovaRecensione.setValutazione(valutazione);
        RecensioneDAO.save(nuovaRecensione);

        final FilmBean film = FilmDAO.findById(idFilm); // Risolto: final
        final List<RecensioneBean> recensioni = RecensioneDAO.findByIdFilm(idFilm); // Risolto: final

        if (film != null) {
            if (recensioni != null && recensioni.isEmpty()) {
                film.setValutazione(0);
            } else if (recensioni != null) {
                int somma = 0;
                for (final RecensioneBean recensioneFilm : recensioni) {
                    if (recensioneFilm != null) {
                        somma += recensioneFilm.getValutazione();
                    }
                }
                final int media = somma / recensioni.size(); // Risolto: final
                film.setValutazione(media);
            }
            FilmDAO.update(film);
        }
    }

    //@ requires email != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<RecensioneBean> FindRecensioni(final String email) {
        final List<RecensioneBean> recensioni = RecensioneDAO.findByUser(email); // Risolto: final
        return recensioni != null ? recensioni : new ArrayList<RecensioneBean>();
    }
    
    //@ requires email != null;
    //@ requires ID_Film >= 0;
    //@ assignable \everything;
    public synchronized void deleteRecensione(final String email, final int ID_Film) {
        ReportDAO.deleteReports(email, ID_Film);
        ValutazioneDAO.deleteValutazioni(email, ID_Film);
        RecensioneDAO.delete(email, ID_Film);

        final FilmBean film = FilmDAO.findById(ID_Film); // Risolto: final
        final List<RecensioneBean> recensioni = RecensioneDAO.findByIdFilm(ID_Film); // Risolto: final

        if (film != null) {
            if (recensioni != null && recensioni.isEmpty()) {
                film.setValutazione(1); 
            } else if (recensioni != null) {
                int somma = 0;
                for (final RecensioneBean recensionefilm : recensioni) {
                    if (recensionefilm != null) {
                        somma += recensionefilm.getValutazione();
                    }
                }
                final int media = somma / recensioni.size(); // Risolto: final
                film.setValutazione(media);
            }
            FilmDAO.update(film);
        }
    }

    //@ requires email != null;
    //@ requires ID_Film >= 0;
    //@ assignable \everything;
    public void deleteReports(final String email, final int ID_Film) {
        final RecensioneBean recensione = RecensioneDAO.findById(email, ID_Film); // Risolto: final
        if (recensione != null) {
            recensione.setNReports(0);
            RecensioneDAO.update(recensione);
        }
        
        ReportDAO.deleteReports(email, ID_Film);
    }
    
    //@ requires ID_film >= 0;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<RecensioneBean> GetRecensioni(final int ID_film){
        final List<RecensioneBean> result = RecensioneDAO.findByIdFilm(ID_film); // Risolto: final
        return result != null ? result : new ArrayList<RecensioneBean>();
    }
    
    //@ requires ID_film >= 0;
    //@ requires email != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public HashMap<String, ValutazioneBean> GetValutazioni(final int ID_film, final String email){
        final HashMap<String, ValutazioneBean> result = ValutazioneDAO.findByIdFilmAndEmail(ID_film, email); // Risolto: final
        return result != null ? result : new HashMap<String, ValutazioneBean>();
    }
    
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<RecensioneBean> GetAllRecensioniSegnalate(){
        final List<RecensioneBean> recensioni = RecensioneDAO.findAll(); // Risolto: final
        final List<RecensioneBean> recensioniFiltered = new ArrayList<RecensioneBean>(); // Risolto: final
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
    public synchronized void report(final String email, final String emailRecensore, final int idFilm) {
        
        final ReportBean report = new ReportBean(); // Risolto: final
        report.setEmailRecensore(emailRecensore);
        report.setEmail(email);
        report.setIdFilm(idFilm);
        if(ReportDAO.findById(email, emailRecensore, idFilm)==null) {
            
            final RecensioneBean recensione = RecensioneDAO.findById(emailRecensore, idFilm); // Risolto: final
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
    public RecensioneBean getRecensione(final int filmId, final String email) { // Parametri final
        final model.DAO.RecensioneDAO localRecensioneDAO = new model.DAO.RecensioneDAO(); // Risolto: final
        return localRecensioneDAO.findById(email, filmId);
    }
}