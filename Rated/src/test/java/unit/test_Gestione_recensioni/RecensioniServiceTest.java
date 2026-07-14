package unit.test_Gestione_recensioni;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import model.DAO.FilmDAO;
import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.ValutazioneDAO;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.ReportBean;
import model.Entity.ValutazioneBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecensioniServiceTest {

    private RecensioniService recensioniService;
    private RecensioneDAO mockRecensioneDAO;
    private ValutazioneDAO mockValutazioneDAO;
    private ReportDAO mockReportDAO;
    private FilmDAO mockFilmDAO;

    @BeforeEach
    void setUp() {
        mockRecensioneDAO = mock(RecensioneDAO.class);
        mockValutazioneDAO = mock(ValutazioneDAO.class);
        mockReportDAO = mock(ReportDAO.class);
        mockFilmDAO = mock(FilmDAO.class);

        // Inietta tutti i DAO mockati tramite il costruttore
        recensioniService = new RecensioniService(mockRecensioneDAO, mockValutazioneDAO, mockReportDAO, mockFilmDAO);
    }

    @Test
    void testAddRecensione() {
        final String email = "user@example.com";
        final int idFilm = 1;
        final String contenuto = "Great movie!";
        final String titolo = "My Review";
        final int valutazione = 5;

        when(mockRecensioneDAO.findById(email, idFilm)).thenReturn(null);
        when(mockFilmDAO.findById(idFilm)).thenReturn(new FilmBean());
        when(mockRecensioneDAO.findByIdFilm(idFilm)).thenReturn(new ArrayList<>());

        recensioniService.addRecensione(email, idFilm, contenuto, titolo, valutazione);

        verify(mockRecensioneDAO).save(any(RecensioneBean.class));
        verify(mockFilmDAO).update(any(FilmBean.class));
    }

    @Test
    void testDeleteRecensione() {
        final String email = "user@example.com";
        final int idFilm = 1;

        final FilmBean film = new FilmBean();
        film.setIdFilm(idFilm);
        when(mockFilmDAO.findById(idFilm)).thenReturn(film);
        when(mockRecensioneDAO.findByIdFilm(idFilm)).thenReturn(new ArrayList<>());

        recensioniService.deleteRecensione(email, idFilm);

        verify(mockRecensioneDAO).delete(email, idFilm);
        verify(mockValutazioneDAO).deleteValutazioni(email, idFilm);
        verify(mockReportDAO).deleteReports(email, idFilm);
        verify(mockFilmDAO).update(film);
    }

    @Test
    void testAddValutazione_New() {
        final String email = "user@example.com";
        final int idFilm = 1;
        final String emailRecensore = "reviewer@example.com";
        final boolean nuovaValutazione = true;

        final RecensioneBean recensione = new RecensioneBean();
        recensione.setNLike(0);
        recensione.setNDislike(0);
        when(mockRecensioneDAO.findById(emailRecensore, idFilm)).thenReturn(recensione);
        when(mockValutazioneDAO.findById(email, emailRecensore, idFilm)).thenReturn(null);

        recensioniService.addValutazione(email, idFilm, emailRecensore, nuovaValutazione);

        verify(mockValutazioneDAO).save(any(ValutazioneBean.class));
        verify(mockRecensioneDAO).update(recensione);
        assertEquals(1, recensione.getNLike());
    }

    @Test
    void testAddValutazione_SwitchToDislike() {
        final String email = "user@example.com";
        final int idFilm = 1;
        final String emailRecensore = "reviewer@example.com";

        final RecensioneBean recensione = new RecensioneBean();
        recensione.setNLike(2);
        recensione.setNDislike(0);
        when(mockRecensioneDAO.findById(emailRecensore, idFilm)).thenReturn(recensione);

        final ValutazioneBean valutazioneEsistente = new ValutazioneBean();
        valutazioneEsistente.setEmail(email);
        valutazioneEsistente.setEmailRecensore(emailRecensore);
        valutazioneEsistente.setIdFilm(idFilm);
        valutazioneEsistente.setLikeDislike(true);
        when(mockValutazioneDAO.findById(email, emailRecensore, idFilm)).thenReturn(valutazioneEsistente);

        recensioniService.addValutazione(email, idFilm, emailRecensore, false);

        assertEquals(1, recensione.getNLike());
        assertEquals(1, recensione.getNDislike());
        assertFalse(valutazioneEsistente.isLikeDislike());
        verify(mockValutazioneDAO).save(valutazioneEsistente);
        verify(mockRecensioneDAO).update(recensione);
    }

    @Test
    void testAddValutazione_RemoveExistingLike() {
        final String email = "user@example.com";
        final int idFilm = 1;
        final String emailRecensore = "reviewer@example.com";

        final RecensioneBean recensione = new RecensioneBean();
        recensione.setNLike(1);
        recensione.setNDislike(0);
        when(mockRecensioneDAO.findById(emailRecensore, idFilm)).thenReturn(recensione);

        final ValutazioneBean valutazioneEsistente = new ValutazioneBean();
        valutazioneEsistente.setEmail(email);
        valutazioneEsistente.setEmailRecensore(emailRecensore);
        valutazioneEsistente.setIdFilm(idFilm);
        valutazioneEsistente.setLikeDislike(true);
        when(mockValutazioneDAO.findById(email, emailRecensore, idFilm)).thenReturn(valutazioneEsistente);

        recensioniService.addValutazione(email, idFilm, emailRecensore, true);

        assertEquals(0, recensione.getNLike());
        verify(mockValutazioneDAO).delete(email, emailRecensore, idFilm);
        verify(mockRecensioneDAO).update(recensione);
    }

    @Test
    void testAddValutazione_RecensioneMissing() {
        final String email = "user@example.com";
        final int idFilm = 1;
        final String emailRecensore = "reviewer@example.com";

        when(mockRecensioneDAO.findById(emailRecensore, idFilm)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> recensioniService.addValutazione(email, idFilm, emailRecensore, true));
    }

    @Test
    void testAddRecensione_DuplicateDoesNotSave() {
        final String email = "user@example.com";
        final int idFilm = 1;

        when(mockRecensioneDAO.findById(email, idFilm)).thenReturn(new RecensioneBean());

        recensioniService.addRecensione(email, idFilm, "contenuto", "titolo", 4);

        verify(mockRecensioneDAO, never()).save(any(RecensioneBean.class));
        verify(mockFilmDAO, never()).update(any(FilmBean.class));
    }

    @Test
    void testFindRecensioni() {
        final String email = "user@example.com";
        final List<RecensioneBean> mockRecensioni = new ArrayList<>();
        mockRecensioni.add(new RecensioneBean());

        when(mockRecensioneDAO.findByUser(email)).thenReturn(mockRecensioni);

        final List<RecensioneBean> result = recensioniService.FindRecensioni(email);

        assertEquals(1, result.size());
        assertSame(mockRecensioni, result);
    }

    @Test
    void testGetAllRecensioniSegnalate() {
        final List<RecensioneBean> allRecensioni = new ArrayList<>();
        final RecensioneBean recensione1 = new RecensioneBean();
        recensione1.setNReports(0);
        final RecensioneBean recensione2 = new RecensioneBean();
        recensione2.setNReports(1);
        allRecensioni.add(recensione1);
        allRecensioni.add(recensione2);

        when(mockRecensioneDAO.findAll()).thenReturn(allRecensioni);

        final List<RecensioneBean> result = recensioniService.GetAllRecensioniSegnalate();

        assertEquals(1, result.size());
        assertSame(recensione2, result.get(0));
    }

    @Test
    void testDeleteReports() {
        final String email = "reviewer@example.com";
        final int idFilm = 1;

        final RecensioneBean recensione = new RecensioneBean();
        recensione.setNReports(3);
        when(mockRecensioneDAO.findById(email, idFilm)).thenReturn(recensione);

        recensioniService.deleteReports(email, idFilm);

        assertEquals(0, recensione.getNReports());
        verify(mockRecensioneDAO).update(recensione);
        verify(mockReportDAO).deleteReports(email, idFilm);
    }

    @Test
    void testGetRecensioni() {
        final int idFilm = 5;
        final List<RecensioneBean> recensioni = new ArrayList<>();
        recensioni.add(new RecensioneBean());
        when(mockRecensioneDAO.findByIdFilm(idFilm)).thenReturn(recensioni);

        final List<RecensioneBean> result = recensioniService.GetRecensioni(idFilm);

        assertSame(recensioni, result);
    }

    @Test
    void testGetValutazioni() {
        final int idFilm = 2;
        final String email = "user@example.com";
        final java.util.HashMap<String, ValutazioneBean> valutazioni = new java.util.HashMap<>();
        valutazioni.put("reviewer@example.com", new ValutazioneBean());
        when(mockValutazioneDAO.findByIdFilmAndEmail(idFilm, email)).thenReturn(valutazioni);

        final java.util.HashMap<String, ValutazioneBean> result = recensioniService.GetValutazioni(idFilm, email);

        assertSame(valutazioni, result);
    }

    @Test
    void testReport() {
        final String email = "user@example.com";        // The user reporting the review
        final String emailRecensore = "reviewer@example.com"; // The author of the review
        final int idFilm = 1;

        // 1. Mock that the user hasn't reported this review yet
        when(mockReportDAO.findById(email, emailRecensore, idFilm)).thenReturn(null);

        // 2. FIX: Mock the existence of the review being reported
        final RecensioneBean recensioneTarget = new RecensioneBean();
        recensioneTarget.setNReports(0); 
        
        when(mockRecensioneDAO.findById(emailRecensore, idFilm)).thenReturn(recensioneTarget);

        // Action
        recensioniService.report(email, emailRecensore, idFilm);

        // Verify
        verify(mockReportDAO).save(any(ReportBean.class));
        
        // Verify that the review's report count was actually updated
        verify(mockRecensioneDAO).update(recensioneTarget); 
    }
    @Test
    void addValutazioneCoversRemainingLikeAndDislikeTransitions() {
        RecensioneBean review = new RecensioneBean();
        review.setNLike(2);
        review.setNDislike(2);
        when(mockRecensioneDAO.findById("reviewer@example.com", 1)).thenReturn(review);

        ValutazioneBean dislike = new ValutazioneBean();
        dislike.setLikeDislike(false);
        when(mockValutazioneDAO.findById("user@example.com", "reviewer@example.com", 1)).thenReturn(dislike);
        recensioniService.addValutazione("user@example.com", 1, "reviewer@example.com", true);
        assertEquals(3, review.getNLike());
        assertEquals(1, review.getNDislike());

        ValutazioneBean removableDislike = new ValutazioneBean();
        removableDislike.setLikeDislike(false);
        when(mockValutazioneDAO.findById("other@example.com", "reviewer@example.com", 1))
                .thenReturn(removableDislike);
        recensioniService.addValutazione("other@example.com", 1, "reviewer@example.com", false);
        assertEquals(0, review.getNDislike());

        when(mockValutazioneDAO.findById("new@example.com", "reviewer@example.com", 1)).thenReturn(null);
        recensioniService.addValutazione("new@example.com", 1, "reviewer@example.com", false);
        assertEquals(1, review.getNDislike());
    }

    @Test
    void addRecensioneCoversMissingFilmListAndNullElementBranches() {
        when(mockRecensioneDAO.findById(anyString(), anyInt())).thenReturn(null);
        recensioniService.addRecensione("a@example.com", 1, "text", "title", 5);

        FilmBean noReviewsFilm = new FilmBean();
        when(mockFilmDAO.findById(2)).thenReturn(noReviewsFilm);
        when(mockRecensioneDAO.findByIdFilm(2)).thenReturn(null);
        recensioniService.addRecensione("b@example.com", 2, "text", "title", 5);

        FilmBean ratedFilm = new FilmBean();
        RecensioneBean review = new RecensioneBean();
        review.setValutazione(4);
        when(mockFilmDAO.findById(3)).thenReturn(ratedFilm);
        when(mockRecensioneDAO.findByIdFilm(3)).thenReturn(java.util.Arrays.asList(null, review));
        recensioniService.addRecensione("c@example.com", 3, "text", "title", 5);
        assertEquals(2, ratedFilm.getValutazione());
    }

    @Test
    void deleteRecensioneCoversMissingFilmListAndNullElementBranches() {
        recensioniService.deleteRecensione("a@example.com", 1);

        FilmBean noReviewsFilm = new FilmBean();
        when(mockFilmDAO.findById(2)).thenReturn(noReviewsFilm);
        when(mockRecensioneDAO.findByIdFilm(2)).thenReturn(null);
        recensioniService.deleteRecensione("b@example.com", 2);

        FilmBean ratedFilm = new FilmBean();
        RecensioneBean review = new RecensioneBean();
        review.setValutazione(4);
        when(mockFilmDAO.findById(3)).thenReturn(ratedFilm);
        when(mockRecensioneDAO.findByIdFilm(3)).thenReturn(java.util.Arrays.asList(null, review));
        recensioniService.deleteRecensione("c@example.com", 3);
        assertEquals(2, ratedFilm.getValutazione());
    }

    @Test
    void nullableQueryResultsAlwaysBecomeEmptyCollections() {
        assertTrue(recensioniService.FindRecensioni("user@example.com").isEmpty());
        assertTrue(recensioniService.GetRecensioni(1).isEmpty());
        assertTrue(recensioniService.GetValutazioni(1, "user@example.com").isEmpty());
        assertTrue(recensioniService.GetAllRecensioniSegnalate().isEmpty());
    }

    @Test
    void filtersNullReviewsAndCoversMissingDeleteReportReview() {
        RecensioneBean reported = new RecensioneBean();
        reported.setNReports(1);
        when(mockRecensioneDAO.findAll()).thenReturn(java.util.Arrays.asList(null, reported));
        assertEquals(List.of(reported), recensioniService.GetAllRecensioniSegnalate());

        recensioniService.deleteReports("missing@example.com", 7);
        verify(mockReportDAO).deleteReports("missing@example.com", 7);
    }

    @Test
    void reportIgnoresDuplicatesAndStillSavesWhenReviewIsMissing() {
        ReportBean duplicate = new ReportBean();
        when(mockReportDAO.findById("user@example.com", "reviewer@example.com", 1)).thenReturn(duplicate);
        recensioniService.report("user@example.com", "reviewer@example.com", 1);
        verify(mockReportDAO, never()).save(any(ReportBean.class));

        when(mockReportDAO.findById("other@example.com", "missing@example.com", 2)).thenReturn(null);
        recensioniService.report("other@example.com", "missing@example.com", 2);
        verify(mockReportDAO).save(any(ReportBean.class));
    }
}
