package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import integration.DatabaseSetupForTest;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import utilities.PasswordUtility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AutenticationServiceIntegrationTest {

    private static DataSource testDataSource;
    private AutenticationService authService;
    private UtenteDAO utenteDAO;

    // 1. Configurazione del Database (Eseguita una sola volta)
    @BeforeAll
    static void beforeAll() {
    	testDataSource = DatabaseSetupForTest.getH2DataSource();
    }

    // 2. Setup prima di ogni singolo test
    @BeforeEach
    void setUp() {
        // IMPORTANTE: Passiamo il DataSource al DAO, non usiamo il costruttore vuoto!
        utenteDAO = new UtenteDAO(testDataSource);
        authService = new AutenticationService(utenteDAO);
        
        // Pulizia preventiva per assicurare che gli utenti di test non esistano
        cleanDb("nuovo.utente@example.com");
    }

    @AfterEach
    void tearDown() {
        // Pulizia dopo il test
        cleanDb("nuovo.utente@example.com");
    }

    // Metodo helper per pulire il DB dai dati di test
    private void cleanDb(final String email) {
        try (final Connection conn = testDataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement("DELETE FROM Utente_Registrato WHERE email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testRegister_NewUser_ShouldSucceed() {
        // ESEMPIO “TC07.1”: registrazione con successo
        final UtenteBean user = authService.register("NuovoUtente", "nuovo.utente@example.com", 
                                               "passwordSicura123", "Bio di test", null);
        
        assertNotNull(user, "La registrazione deve restituire un utente non nullo.");
        assertEquals("NuovoUtente", user.getUsername());
        assertEquals("RECENSORE", user.getTipoUtente(), "Tipo utente di default dev’essere 'RECENSORE'.");
        
        // Verifica extra: l'utente è davvero nel DB?
        assertNotNull(utenteDAO.findByEmail("nuovo.utente@example.com"));
    }

    @Test
    void testRegister_ExistingEmail_ShouldFail() {
        // Preparazione: Inserisco manualmente un utente nel DB
        final UtenteBean existing = new UtenteBean();
        existing.setEmail("nuovo.utente@example.com"); // Uso la stessa email del test sopra per comodità
        existing.setUsername("alice.rossi");
        existing.setPassword(PasswordUtility.hashPassword("alice123"));
        existing.setTipoUtente("RECENSORE");
        utenteDAO.save(existing);

        // Azione: Provo a registrare un NUOVO utente con la STESSA email
        final UtenteBean result = authService.register("AltroNome", "nuovo.utente@example.com", 
                                                 "pass123", "Bio", null);
        
        // Verifica
        assertNull(result, "Se l'email è già presente, la registrazione deve fallire (null).");
    }

    @Test
    void testLogin_CorrectCredentials_ShouldReturnUser() {
        // Preparazione: Creo l'utente
        final String email = "nuovo.utente@example.com";
        final String pass = "chiara123";
        
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("chiara.neri");
        user.setPassword(PasswordUtility.hashPassword(pass)); // Importante: Salvare la password HASHATA
        user.setTipoUtente("RECENSORE");
        utenteDAO.save(user);

        // Azione: Login con password IN CHIARO
        final UtenteBean loggedUser = authService.login(email, pass);
        
        // Verifica
        assertNotNull(loggedUser, "Login deve avere successo.");
        assertEquals("chiara.neri", loggedUser.getUsername());
    }

    @Test
    void testLogin_WrongPassword_ShouldReturnNull() {
        // Preparazione
        final String email = "nuovo.utente@example.com";
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setUsername("alice.rossi");
        user.setPassword(PasswordUtility.hashPassword("alice123"));
        utenteDAO.save(user);

        // Azione: Password sbagliata
        final UtenteBean result = authService.login(email, "wrongPass");
        
        // Verifica
        assertNull(result, "Login deve fallire con password sbagliata.");
    }
}