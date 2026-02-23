package integration.test_Gestione_recensioni;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import integration.DatabaseSetupForTest;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.UtenteDAO;
import model.DAO.FilmDAO;
import model.DAO.ValutazioneDAO;
import model.Entity.RecensioneBean;
import model.Entity.ReportBean;
import model.Entity.UtenteBean;
import model.Entity.FilmBean;
import model.Entity.ValutazioneBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

public class RecensioniServiceIntegrationTest {

    private static DataSource testDataSource;

    private RecensioneDAO recensioneDAO;
    private UtenteDAO utenteDAO;
    private ReportDAO reportDAO;
    private FilmDAO filmDAO;
    private ValutazioneDAO valutazioneDAO;
    private RecensioniService recensioniService;

    @BeforeAll
    static void beforeAll() {
        testDataSource = DatabaseSetupForTest.getH2DataSource();
    }

    @BeforeEach
    void setUp() {
        recensioneDAO = new RecensioneDAO(testDataSource);
        filmDAO = new FilmDAO(testDataSource);
        valutazioneDAO = new ValutazioneDAO(testDataSource);
        utenteDAO = new UtenteDAO(testDataSource);
        reportDAO = new ReportDAO(testDataSource);
        recensioniService = new RecensioniService(recensioneDAO, valutazioneDAO, reportDAO, filmDAO);
        
        try (final Connection conn = testDataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
               stmt.executeUpdate("DELETE FROM Valutazione");
               stmt.executeUpdate("DELETE FROM Report");
               stmt.executeUpdate("DELETE FROM Recensione");
               stmt.executeUpdate("DELETE FROM Film");
               stmt.executeUpdate("DELETE FROM Utente_Registrato");
           } catch (SQLException e) {
               e.printStackTrace();
           }
    }

    @Test
    void testAddRecensione_Valid_ShouldCreateAndUpdateFilmRating() {
        final UtenteBean alice = new UtenteBean();
        alice.setEmail("alice@example.com");
        alice.setUsername("Alice");
        alice.setPassword("password");
        utenteDAO.save(alice);
        
        FilmBean film = new FilmBean();
        film.setNome("FilmTest");
        filmDAO.save(film);
        film = filmDAO.findByName("FilmTest").get(0);
        
        recensioniService.addRecensione("alice@example.com", film.getIdFilm(), "Ottimo film", "Recensione Alice", 5);

        final RecensioneBean rec = recensioneDAO.findById("alice@example.com", film.getIdFilm());
        assertNotNull(rec);
        assertEquals("Recensione Alice", rec.getTitolo());
        assertEquals(5, rec.getValutazione());

        final FilmBean updatedFilm = filmDAO.findById(film.getIdFilm());
        assertEquals(5, updatedFilm.getValutazione());
    }

    @Test
    void testAddRecensione_Duplicate_ShouldNotCreate() {
        final UtenteBean bob = new UtenteBean();
        bob.setEmail("bob@example.com");
        bob.setUsername("bob");
        bob.setPassword("password");
        utenteDAO.save(bob);
        
        FilmBean film = new FilmBean();
        film.setNome("Film DoubleRec");
        filmDAO.save(film);
        film = filmDAO.findByName("Film DoubleRec").get(0);
        
        recensioniService.addRecensione("bob@example.com", film.getIdFilm(), "Prima", "Titolo1", 3);
        recensioniService.addRecensione("bob@example.com", film.getIdFilm(), "Seconda", "Titolo2", 5);

        final List<RecensioneBean> recs = recensioneDAO.findByIdFilm(film.getIdFilm());
        assertEquals(1, recs.size());
        assertEquals("Prima", recs.get(0).getContenuto());
    }

    @Test
    void testAddValutazione_NewLike_ShouldIncrementNLike() {
        final UtenteBean y = new UtenteBean();
        y.setEmail("y@example.com");
        y.setUsername("y");
        y.setPassword("password");
        utenteDAO.save(y);
        
        final UtenteBean x = new UtenteBean();
        x.setEmail("x@example.com");
        x.setUsername("x");
        x.setPassword("password");
        utenteDAO.save(x);
        
        FilmBean film = new FilmBean();
        film.setNome("FilmLikeTest");
        filmDAO.save(film);
        film = filmDAO.findByName("FilmLikeTest").get(0);

        recensioniService.addRecensione("y@example.com", film.getIdFilm(), "Rec di Y", "Titolo Rec Y", 3);

        recensioniService.addValutazione("x@example.com", film.getIdFilm(), "y@example.com", true);

        final RecensioneBean recY = recensioneDAO.findById("y@example.com", film.getIdFilm());
        assertEquals(1, recY.getNLike());
        assertEquals(0, recY.getNDislike());
    }

    @Test
    void testDeleteRecensione_ShouldRemoveAndUpdateFilmRating() {
        final UtenteBean bob = new UtenteBean();
        bob.setEmail("bob@example.com");
        bob.setUsername("bob");
        bob.setPassword("password");
        utenteDAO.save(bob);
        
        final UtenteBean alice = new UtenteBean();
        alice.setEmail("alice@example.com");
        alice.setUsername("Alice");
        alice.setPassword("password");
        utenteDAO.save(alice);
        
        FilmBean film = new FilmBean();
        film.setNome("Film DoubleRec");
        filmDAO.save(film);
        film = filmDAO.findByName("Film DoubleRec").get(0);

        recensioniService.addRecensione("alice@example.com", film.getIdFilm(), "Rec Alice", "Tit1", 4);
        recensioniService.addRecensione("bob@example.com", film.getIdFilm(), "Rec Bob", "Tit2", 2);

        recensioniService.deleteRecensione("alice@example.com", film.getIdFilm());

        assertNull(recensioneDAO.findById("alice@example.com", film.getIdFilm()));

        final FilmBean updatedFilm = filmDAO.findById(film.getIdFilm());
        assertEquals(2, updatedFilm.getValutazione());
    }

    @Test
    void testReportAndDeleteReports() {
        final UtenteBean reporter = new UtenteBean();
        reporter.setEmail("reporter@example.com");
        reporter.setUsername("reporter");
        reporter.setPassword("password");
        utenteDAO.save(reporter);

        final UtenteBean author = new UtenteBean();
        author.setEmail("author@example.com");
        author.setUsername("author");
        author.setPassword("password");
        utenteDAO.save(author);

        FilmBean film = new FilmBean();
        film.setNome("Film Report");
        filmDAO.save(film);
        film = filmDAO.findByName("Film Report").get(0);

        recensioniService.addRecensione(author.getEmail(), film.getIdFilm(), "Recensione", "Titolo", 4);

        recensioniService.report(reporter.getEmail(), author.getEmail(), film.getIdFilm());

        RecensioneBean recensione = recensioneDAO.findById(author.getEmail(), film.getIdFilm());
        assertEquals(1, recensione.getNReports());

        recensioniService.deleteReports(author.getEmail(), film.getIdFilm());
        recensione = recensioneDAO.findById(author.getEmail(), film.getIdFilm());
        assertEquals(0, recensione.getNReports());
    }
    
    @Test
    void testFindRecensioniByUser() {
        final UtenteBean user = new UtenteBean();
        user.setEmail("reviewer@example.com");
        user.setUsername("Reviewer");
        user.setPassword("password");
        utenteDAO.save(user);

        FilmBean film1 = new FilmBean();
        film1.setNome("Film Uno");
        filmDAO.save(film1);
        film1 = filmDAO.findByName("Film Uno").get(0);

        recensioniService.addRecensione(user.getEmail(), film1.getIdFilm(), "Recensione 1", "Titolo 1", 4);

        List<RecensioneBean> results = recensioniService.FindRecensioni(user.getEmail());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(user.getEmail(), results.get(0).getEmail());
    }

    @Test
    void testGetAllRecensioniSegnalate() {
        final UtenteBean author = new UtenteBean();
        author.setEmail("bad_author@example.com");
        author.setUsername("BadAuthor");
        author.setPassword("pass");
        utenteDAO.save(author);
        
        final UtenteBean reporter = new UtenteBean();
        reporter.setEmail("reporter@example.com");
        reporter.setUsername("Reporter");
        reporter.setPassword("pass");
        utenteDAO.save(reporter);

        FilmBean film = new FilmBean();
        film.setNome("Film Controverso");
        filmDAO.save(film);
        film = filmDAO.findByName("Film Controverso").get(0);

        recensioniService.addRecensione(author.getEmail(), film.getIdFilm(), "Contenuto offensivo", "Titolo", 1);
        recensioniService.report(reporter.getEmail(), author.getEmail(), film.getIdFilm());

        List<RecensioneBean> segnalate = recensioniService.GetAllRecensioniSegnalate();

        assertNotNull(segnalate);
        assertFalse(segnalate.isEmpty());
        assertTrue(segnalate.stream().anyMatch(r -> r.getEmail().equals(author.getEmail())));
    }
    
    @Test
    void testGetValutazioni_Service() {
        // 1. Setup Utenti
        final UtenteBean liker = new UtenteBean();
        liker.setEmail("liker@test.com");
        liker.setUsername("Liker");
        liker.setPassword("pass");
        utenteDAO.save(liker);
        
        final UtenteBean reviewer = new UtenteBean();
        reviewer.setEmail("reviewer@test.com");
        reviewer.setUsername("Reviewer");
        reviewer.setPassword("pass");
        utenteDAO.save(reviewer);
        
        // 2. Setup Film e Recensione
        FilmBean film = new FilmBean();
        film.setNome("Film Service Valutazioni");
        filmDAO.save(film);
        film = filmDAO.findByName("Film Service Valutazioni").get(0);
        
        recensioniService.addRecensione(reviewer.getEmail(), film.getIdFilm(), "Content", "Title", 5);
        
        // 3. Aggiungo Like tramite Service
        recensioniService.addValutazione(liker.getEmail(), film.getIdFilm(), reviewer.getEmail(), true);
        
        // 4. Test del metodo del Service GetValutazioni (che chiama il DAO)
        HashMap<String, ValutazioneBean> valutazioni = recensioniService.GetValutazioni(film.getIdFilm(), liker.getEmail());
        
        assertNotNull(valutazioni);
        assertTrue(valutazioni.containsKey(reviewer.getEmail()), "La mappa dovrebbe contenere la valutazione fatta al reviewer");
        ValutazioneBean v = valutazioni.get(reviewer.getEmail());
        assertTrue(v.isLikeDislike());
    }

    // --- NUOVI TEST AGGIUNTI PER AUMENTARE LA COVERAGE (Senza metodi helper) ---

    @Test
    void testAddRecensione_AggiornaMediaFilm() {
        // Setup Utenti
        UtenteBean u1 = new UtenteBean();
        u1.setEmail("u1@test.com");
        u1.setUsername("U1");
        u1.setPassword("pass");
        utenteDAO.save(u1);
        
        UtenteBean u2 = new UtenteBean();
        u2.setEmail("u2@test.com");
        u2.setUsername("U2");
        u2.setPassword("pass");
        utenteDAO.save(u2);
        
        // Setup Film
        FilmBean film = new FilmBean();
        film.setNome("Film Media");
        filmDAO.save(film);
        film = filmDAO.findByName("Film Media").get(0);
        
        // Act 1: Prima recensione (voto 4)
        recensioniService.addRecensione(u1.getEmail(), film.getIdFilm(), "Rec1", "Tit1", 4);
        FilmBean f1 = filmDAO.findById(film.getIdFilm());
        assertEquals(4, f1.getValutazione());
        
        // Act 2: Seconda recensione (voto 2) -> Media attesa (4+2)/2 = 3
        recensioniService.addRecensione(u2.getEmail(), film.getIdFilm(), "Rec2", "Tit2", 2);
        FilmBean f2 = filmDAO.findById(film.getIdFilm());
        assertEquals(3, f2.getValutazione());
    }

    @Test
    void testAddValutazione_ToggleLike_ShouldRemove() {
        // Setup Utenti
        UtenteBean liker = new UtenteBean();
        liker.setEmail("liker@test.com");
        liker.setUsername("Liker");
        liker.setPassword("pass");
        utenteDAO.save(liker);

        UtenteBean reviewer = new UtenteBean();
        reviewer.setEmail("rev@test.com");
        reviewer.setUsername("Reviewer");
        reviewer.setPassword("pass");
        utenteDAO.save(reviewer);

        // Setup Film
        FilmBean film = new FilmBean();
        film.setNome("Film Toggle");
        filmDAO.save(film);
        film = filmDAO.findByName("Film Toggle").get(0);
        
        // Setup Recensione
        recensioniService.addRecensione(reviewer.getEmail(), film.getIdFilm(), "Contenuto", "Titolo", 3);
        
        // Act 1: Aggiungo Like
        recensioniService.addValutazione(liker.getEmail(), film.getIdFilm(), reviewer.getEmail(), true);
        RecensioneBean r1 = recensioneDAO.findById(reviewer.getEmail(), film.getIdFilm());
        assertEquals(1, r1.getNLike());
        
        // Act 2: Rimetto lo stesso Like -> Dovrebbe rimuoverlo (Toggle)
        recensioniService.addValutazione(liker.getEmail(), film.getIdFilm(), reviewer.getEmail(), true);
        RecensioneBean r2 = recensioneDAO.findById(reviewer.getEmail(), film.getIdFilm());
        assertEquals(0, r2.getNLike());
        
        // Verifica che il record in Valutazione sia stato cancellato
        assertNull(valutazioneDAO.findById(liker.getEmail(), reviewer.getEmail(), film.getIdFilm()));
    }

    @Test
    void testAddValutazione_ChangeVote_ShouldSwitchCounters() {
        // Setup Utenti
        UtenteBean voter = new UtenteBean();
        voter.setEmail("voter@test.com");
        voter.setUsername("Voter");
        voter.setPassword("pass");
        utenteDAO.save(voter);

        UtenteBean reviewer = new UtenteBean();
        reviewer.setEmail("rev2@test.com");
        reviewer.setUsername("Rev2");
        reviewer.setPassword("pass");
        utenteDAO.save(reviewer);

        // Setup Film
        FilmBean film = new FilmBean();
        film.setNome("Film Switch");
        filmDAO.save(film);
        film = filmDAO.findByName("Film Switch").get(0);
        
        // Setup Recensione
        recensioniService.addRecensione(reviewer.getEmail(), film.getIdFilm(), "C", "T", 3);
        
        // Act 1: Metto Like
        recensioniService.addValutazione(voter.getEmail(), film.getIdFilm(), reviewer.getEmail(), true);
        
        // Act 2: Cambio in Dislike
        recensioniService.addValutazione(voter.getEmail(), film.getIdFilm(), reviewer.getEmail(), false);
        
        RecensioneBean rec = recensioneDAO.findById(reviewer.getEmail(), film.getIdFilm());
        assertEquals(0, rec.getNLike(), "Like dovrebbe essere 0");
        assertEquals(1, rec.getNDislike(), "Dislike dovrebbe essere 1");
    }

    @Test
    void testGetRecensione_ShouldReturnBean() {
        UtenteBean u = new UtenteBean();
        u.setEmail("getrec@test.com");
        u.setUsername("GetRec");
        u.setPassword("pass");
        utenteDAO.save(u);

        FilmBean f = new FilmBean();
        f.setNome("Film Get");
        filmDAO.save(f);
        f = filmDAO.findByName("Film Get").get(0);

        recensioniService.addRecensione(u.getEmail(), f.getIdFilm(), "C", "T", 5);
        
        RecensioneBean result = recensioniService.getRecensione(f.getIdFilm(), u.getEmail());
        assertNotNull(result);
        assertEquals("T", result.getTitolo());
    }
    
    @Test
    void testFindReportById_ShouldReturnBean() {
        // --- 1. SETUP DATI (Senza metodi helper) ---
        
        // Creazione Utente Reporter (Chi segnala)
        UtenteBean reporter = new UtenteBean();
        reporter.setEmail("reporter_cover@test.com");
        reporter.setUsername("ReporterCover");
        reporter.setPassword("passHash");
        reporter.setTipoUtente("RECENSORE");
        utenteDAO.save(reporter);

        // Creazione Utente Autore (Chi viene segnalato)
        UtenteBean author = new UtenteBean();
        author.setEmail("author_cover@test.com");
        author.setUsername("AuthorCover");
        author.setPassword("passHash");
        author.setTipoUtente("RECENSORE");
        utenteDAO.save(author);

        // Creazione Film
        FilmBean film = new FilmBean();
        film.setNome("Report DAO Test Film");
        film.setAnno(2023);
        film.setDurata(120);
        film.setValutazione(3);
        filmDAO.save(film);
        // Recupero ID generato
        int filmId = filmDAO.findByName("Report DAO Test Film").get(0).getIdFilm();

        // Creazione Recensione (necessaria per poterla segnalare)
        RecensioneBean rec = new RecensioneBean();
        rec.setEmail(author.getEmail());
        rec.setIdFilm(filmId);
        rec.setTitolo("Titolo Offensivo");
        rec.setContenuto("Contenuto da segnalare");
        rec.setValutazione(1);
        recensioneDAO.save(rec);

        // --- 2. ACT ---
        // Usiamo il service per creare il report (simulazione flusso reale)
        recensioniService.report(reporter.getEmail(), author.getEmail(), filmId);

        // --- 3. ASSERT & COVERAGE ---
        // Chiamiamo esplicitamente il metodo DAO findById.
        // Poiché il report ora esiste, rs.next() sarà true e il codice entrerà nell'IF.
        ReportBean result = reportDAO.findById(reporter.getEmail(), author.getEmail(), filmId);

        assertNotNull(result, "Il metodo findById dovrebbe restituire un oggetto ReportBean quando il report esiste");
        assertEquals(reporter.getEmail(), result.getEmail(), "L'email del reporter non corrisponde");
        assertEquals(author.getEmail(), result.getEmailRecensore(), "L'email dell'autore segnalato non corrisponde");
        assertEquals(filmId, result.getIdFilm(), "L'ID del film non corrisponde");
    }
}