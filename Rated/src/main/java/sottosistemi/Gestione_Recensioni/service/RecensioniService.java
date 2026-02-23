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
    public final RecensioneDAO RecensioneDAO; // Reso final
    public final ValutazioneDAO ValutazioneDAO; // Reso final
    public final ReportDAO ReportDAO; // Reso final
    public final FilmDAO FilmDAO; // Reso final

    public RecensioniService() {
        this.RecensioneDAO = new RecensioneDAO();
        this.ValutazioneDAO = new ValutazioneDAO();
        this.ReportDAO = new ReportDAO();
        this.FilmDAO = new FilmDAO();
    }
    
    // Costruttore personalizzato per i test
    public RecensioniService(final RecensioneDAO recensioneDAO, final ValutazioneDAO valutazioneDAO, final ReportDAO reportDAO, final FilmDAO filmDAO) { // Parametri final
        this.RecensioneDAO = recensioneDAO;
        this.ValutazioneDAO = valutazioneDAO;
        this.ReportDAO = reportDAO;
        this.FilmDAO = filmDAO;
    }
    
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

        if (recensioni.isEmpty()) {
            film.setValutazione(0); // Nessuna recensione: valutazione predefinita
        } else {
            int somma = 0;
            for (final RecensioneBean recensioneFilm : recensioni) {
                somma += recensioneFilm.getValutazione();
            }
            final int media = somma / recensioni.size();
            film.setValutazione(media);
        }

        FilmDAO.update(film);
    }

    public List<RecensioneBean> FindRecensioni(final String email) { // Parametro final
    	final List<RecensioneBean> recensioni = RecensioneDAO.findByUser(email);
    	return recensioni;
    }
    
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

        if (recensioni.isEmpty()) {
            // Nessuna recensione rimasta: impostare valutazione al valore minimo consentito
            film.setValutazione(1); // Il valore 1 è il minimo consentito dal CHECK constraint
        } else {
            int somma = 0;
            for (final RecensioneBean recensionefilm : recensioni) {
                somma += recensionefilm.getValutazione();
            }
            final int media = somma / recensioni.size();
            film.setValutazione(media);
        }

        // Aggiorna il film con la nuova valutazione
        FilmDAO.update(film);
    }

    public void deleteReports(final String email, final int ID_Film) { // Parametri final
    	final RecensioneBean recensione = RecensioneDAO.findById(email, ID_Film);
    	recensione.setNReports(0);
    	RecensioneDAO.update(recensione);
    	
    	ReportDAO.deleteReports(email, ID_Film);
    }
    
    public List<RecensioneBean> GetRecensioni(final int ID_film){ // Parametro final
    	return RecensioneDAO.findByIdFilm(ID_film);
    }
    
    public HashMap<String, ValutazioneBean> GetValutazioni(final int ID_film, final String email){ // Parametri final
    	return ValutazioneDAO.findByIdFilmAndEmail(ID_film, email);
    }
    
    public List<RecensioneBean> GetAllRecensioniSegnalate(){
    	final List<RecensioneBean> recensioni = RecensioneDAO.findAll();
    	final List<RecensioneBean> recensioniFiltered = new ArrayList<RecensioneBean>();
    	for(final RecensioneBean recensione : recensioni)
    		if(recensione.getNReports()!=0)
    			recensioniFiltered.add(recensione);
    	
    	return recensioniFiltered;
    }
    
    public synchronized void report(final String email, final String emailRecensore, final int idFilm) { // Parametri final
    	
    	final ReportBean report = new ReportBean();
    	report.setEmailRecensore(emailRecensore);
    	report.setEmail(email);
    	report.setIdFilm(idFilm);
    	if(ReportDAO.findById(email, emailRecensore, idFilm)==null) {
    		
    		final RecensioneBean recensione = RecensioneDAO.findById(emailRecensore, idFilm);
    		recensione.setNReports(recensione.getNReports()+1);
    		RecensioneDAO.update(recensione);
    		ReportDAO.save(report);
    	}
    }
    
    public RecensioneBean getRecensione(int filmId, String email) {
        model.DAO.RecensioneDAO recensioneDAO = new model.DAO.RecensioneDAO();
        return recensioneDAO.findById(email, filmId);
    }
}