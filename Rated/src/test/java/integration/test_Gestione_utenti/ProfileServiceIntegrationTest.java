package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import integration.DatabaseSetupForTest;
import utilities.PasswordUtility;

import sottosistemi.Gestione_Utenti.service.ProfileService;
import model.DAO.UtenteDAO;
import model.DAO.PreferenzaDAO;
import model.DAO.InteresseDAO;
import model.DAO.VistoDAO;
import model.DAO.FilmDAO;
import model.DAO.GenereDAO;
import model.Entity.UtenteBean;
import model.Entity.GenereBean;
import model.Entity.FilmBean; 
import model.Entity.RecensioneBean;
import model.Entity.InteresseBean;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileServiceIntegrationTest {

    private static DataSource testDataSource;

    private UtenteDAO utenteDAO;
    private ProfileService profileService;

    @BeforeAll
    static void beforeAll() {
        testDataSource = DatabaseSetupForTest.getH2DataSource();
    }

    @BeforeEach
    void setUp() {
        utenteDAO = new UtenteDAO(testDataSource);
        profileService = new ProfileService(testDataSource);
    }

    @AfterEach
    void tearDown() {
        // List of all emails used in your tests
        final String[] emailsToDelete = {
            "test@example.com", 
            "a@example.com", 
            "b@example.com", 
            "pw@example.com", 
            "mail1@example.com", 
            "mail2@example.com",
            "watch@example.com",
            "visto@example.com",
            "pref@example.com",
            "intDAO@example.com"
        };

        try (final Connection conn = testDataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try (final PreparedStatement deletePreferenza = conn.prepareStatement("DELETE FROM Preferenza WHERE email = ?");
                 final PreparedStatement deleteInteresse = conn.prepareStatement("DELETE FROM Interesse WHERE email = ?");
                 final PreparedStatement deleteVisto = conn.prepareStatement("DELETE FROM Visto WHERE email = ?");
                 final PreparedStatement deleteUtente = conn.prepareStatement("DELETE FROM Utente_Registrato WHERE email = ?")) {
                for (final String email : emailsToDelete) {
                    deletePreferenza.setString(1, email);
                    deletePreferenza.addBatch();
                    deleteInteresse.setString(1, email);
                    deleteInteresse.addBatch();
                    deleteVisto.setString(1, email);
                    deleteVisto.addBatch();
                    deleteUtente.setString(1, email);
                    deleteUtente.addBatch();
                }
                deletePreferenza.executeBatch();
                deleteInteresse.executeBatch();
                deleteVisto.executeBatch();
                deleteUtente.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void testProfileUpdate_Success() {
        final String email = "test@example.com";
        final UtenteBean existingUser = new UtenteBean();
        existingUser.setEmail(email);
        existingUser.setUsername("olduser");
        existingUser.setPassword(PasswordUtility.hashPassword("oldpass")); 
        existingUser.setBiografia("vecchia bio");
        utenteDAO.save(existingUser);

        final String newUsername = "newUsername";
        final String newPasswordClear = "newPassword"; 
        final String newBio = "New biography";
        final byte[] icon = new byte[]{1,2,3};

        final UtenteBean updatedUser = profileService.ProfileUpdate(newUsername, email, newPasswordClear, newBio, icon);

        assertNotNull(updatedUser);
        assertEquals(newUsername, updatedUser.getUsername());
        
        final String expectedHash = PasswordUtility.hashPassword(newPasswordClear);
        assertEquals(expectedHash, updatedUser.getPassword());

        assertEquals(newBio, updatedUser.getBiografia());
        assertArrayEquals(icon, updatedUser.getIcona());
    }
    
    @Test
    void testProfileUpdate_UsernameAlreadyExists() {
        final UtenteBean userA = new UtenteBean();
        userA.setEmail("a@example.com");
        userA.setUsername("UserA");
        userA.setPassword("passA");
        utenteDAO.save(userA);

        final UtenteBean userB = new UtenteBean();
        userB.setEmail("b@example.com");
        userB.setUsername("UserB");
        userB.setPassword("passB");
        utenteDAO.save(userB);

        final UtenteBean result = profileService.ProfileUpdate("UserA", "b@example.com", "passB", "Bio", null);
        assertNull(result);
    }
    
    @Test
    void testPasswordUpdate_Success() {
        final String email = "pw@example.com";
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("pwUser");
        user.setPassword("oldPass");
        utenteDAO.save(user);

        final String newPasswordClear = "newPass";
        
        final UtenteBean updated = profileService.PasswordUpdate(email, newPasswordClear);
        
        assertNotNull(updated);
        
        final String expectedHash = PasswordUtility.hashPassword(newPasswordClear);
        assertEquals(expectedHash, updated.getPassword());
    }

    @Test
    void testPasswordUpdate_UserNotFound() {
        final UtenteBean result = profileService.PasswordUpdate("nonexistent@example.com", "pass");
        assertNull(result);
    }

    @Test
    void testGetUsers() {
        final List<RecensioneBean> recensioni = new ArrayList<>();

        final RecensioneBean r1 = new RecensioneBean();
        r1.setEmail("mail1@example.com");
        final RecensioneBean r2 = new RecensioneBean();
        r2.setEmail("mail2@example.com");
        recensioni.add(r1);
        recensioni.add(r2);

        final UtenteBean user1 = new UtenteBean();
        user1.setEmail("mail1@example.com");
        user1.setUsername("user1");
        utenteDAO.save(user1);

        final UtenteBean user2 = new UtenteBean();
        user2.setEmail("mail2@example.com");
        user2.setUsername("user2");
        utenteDAO.save(user2);

        final HashMap<String, String> usersMap = profileService.getUsers(recensioni);

        assertEquals(2, usersMap.size());
        assertEquals("user1", usersMap.get("mail1@example.com"));
        assertEquals("user2", usersMap.get("mail2@example.com"));
    }

    @Test
    void testWatchlistLifecycle() {
        final String email = "watch@example.com";
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("watchUser");
        user.setPassword("pass");
        utenteDAO.save(user);

        final FilmDAO filmDAO = new FilmDAO(testDataSource);
        final FilmBean film = new FilmBean();
        film.setNome("Watchlist Film");
        film.setValutazione(3);
        filmDAO.save(film);
        final int filmId = filmDAO.findByName("Watchlist Film").get(0).getIdFilm();
        profileService.aggiungiAllaWatchlist(email, filmId);

        final InteresseDAO interesseDAO = new InteresseDAO(testDataSource);
        final InteresseBean interesse = interesseDAO.findByEmailAndIdFilm(email, filmId);
        assertNotNull(interesse);
        assertTrue(interesse.isInteresse());

        profileService.rimuoviDallaWatchlist(email, filmId);
        assertNull(interesseDAO.findByEmailAndIdFilm(email, filmId));
    }

    @Test
    void testFilmVistoLifecycle() {
        final String email = "visto@example.com";
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("vistoUser");
        user.setPassword("pass");
        utenteDAO.save(user);

        final FilmDAO filmDAO = new FilmDAO(testDataSource);
        final FilmBean film = new FilmBean();
        film.setNome("Visto Film");
        film.setValutazione(3);
        filmDAO.save(film);
        final int filmId = filmDAO.findByName("Visto Film").get(0).getIdFilm();
        profileService.aggiungiFilmVisto(email, filmId);

        assertTrue(profileService.isFilmVisto(email, filmId));

        profileService.rimuoviFilmVisto(email, filmId);
        final VistoDAO vistoDAO = new VistoDAO(testDataSource);
        assertNull(vistoDAO.findByEmailAndIdFilm(email, filmId));
    }

    @Test
    void testAggiornaPreferenzeUtente_Persist() {
        final String email = "pref@example.com";
        
        final GenereDAO genereDAO = new GenereDAO(testDataSource);
        genereDAO.save(new GenereBean("Azione"));
        genereDAO.save(new GenereBean("Drammatico"));
        
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("prefUser");
        user.setPassword("pass");
        utenteDAO.save(user);

        final String[] generi = new String[]{"Azione", "Drammatico"};
        profileService.aggiornaPreferenzeUtente(email, generi);

        final PreferenzaDAO preferenzaDAO = new PreferenzaDAO(testDataSource);
        assertEquals(2, preferenzaDAO.findByEmail(email).size());
    }

    
    @Test
    void testAggiungiFilmVisto_Duplicate_ShouldNotThrowAndNotDuplicate() throws SQLException {
        // --- 1. SETUP DATI (Senza metodi helper) ---
        String email = "duplicate@test.com";
        
        // Creazione Utente
        UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("DupUser");
        user.setPassword("passHash"); 
        user.setTipoUtente("RECENSORE");
        utenteDAO.save(user); // Usa utenteDAO già presente nella classe

        // Creazione Film (Serve FilmDAO o SQL diretto)
        // Istanzio FilmDAO localmente usando il dataSource di test
        model.DAO.FilmDAO filmDAO = new model.DAO.FilmDAO(testDataSource);
        FilmBean film = new FilmBean();
        film.setNome("DupFilm");
        film.setAnno(2023);
        film.setDurata(120);
        film.setValutazione(3);
        filmDAO.save(film); 
        // FilmDAO.save aggiorna l'ID nel bean
        int filmId = film.getIdFilm();

        // --- 2. ACT ---
        // Prima aggiunta
        profileService.aggiungiFilmVisto(email, filmId);
        
        // Seconda aggiunta (dovrebbe essere ignorata dal DAO senza errori)
        assertDoesNotThrow(() -> profileService.aggiungiFilmVisto(email, filmId));

        // --- 3. ASSERT ---
        // Verifichiamo che ci sia 1 solo record nel DB
        List<FilmBean> visti = profileService.retrieveWatchedFilms("DupUser");
        assertEquals(1, visti.size(), "Non dovrebbero esserci duplicati nella lista visti");
    }
    @Test
    void testRetrieveWatchlist() {
        final String email = "retwatch@example.com";
        final String username = "RetWatchUser";
        
        // 1. Setup Utente
        UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword("pass");
        utenteDAO.save(user);
        
        // 2. Setup Film
        FilmDAO filmDAO = new FilmDAO(testDataSource);
        FilmBean film1 = new FilmBean();
        film1.setNome("Film Watch 1");
        film1.setValutazione(3);
        filmDAO.save(film1);
        int id1 = filmDAO.findByName("Film Watch 1").get(0).getIdFilm();
        
        FilmBean film2 = new FilmBean();
        film2.setNome("Film Watch 2");
        film2.setValutazione(4);
        filmDAO.save(film2);
        int id2 = filmDAO.findByName("Film Watch 2").get(0).getIdFilm();
        
        // 3. Aggiunta alla watchlist
        profileService.aggiungiAllaWatchlist(email, id1);
        profileService.aggiungiAllaWatchlist(email, id2);
        
        // 4. Test Metodo
        List<FilmBean> result = profileService.retrieveWatchlist(username);
        
        // 5. Verifiche
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> f.getIdFilm() == id1));
        assertTrue(result.stream().anyMatch(f -> f.getIdFilm() == id2));
    }
    
    
}