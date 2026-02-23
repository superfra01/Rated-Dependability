package integration.test_Gestione_utenti;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import integration.DatabaseSetupForTest;
import sottosistemi.Gestione_Utenti.service.ModerationService;
import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;

import javax.sql.DataSource;

public class ModerationServiceIntegrationTest {

    private static DataSource testDataSource;

    private UtenteDAO utenteDAO;
    private ModerationService moderationService;

    @BeforeAll
    static void beforeAll() {
    	testDataSource = DatabaseSetupForTest.getH2DataSource();
    }

    @BeforeEach
    void setUp() {
        utenteDAO = new UtenteDAO(testDataSource);
        moderationService = new ModerationService(utenteDAO);
    }

    @AfterEach
    void tearDown() {
        try (final java.sql.Connection conn = testDataSource.getConnection();
             final java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM Utente_Registrato WHERE email = ?")) {
            ps.setString(1, "moderation@example.com");
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

   
    @Test
    void testWarn_UserNotFound() {
        assertDoesNotThrow(() -> {
            moderationService.warn("non.esiste@example.com");
        });
    }

    @Test
    void testWarn_UserExists() {
        final UtenteBean user = new UtenteBean();
        user.setEmail("moderation@example.com");
        user.setUsername("moderation");
        user.setPassword("pass");
        user.setNWarning(0);
        utenteDAO.save(user);

        moderationService.warn("moderation@example.com");

        final UtenteBean updated = utenteDAO.findByEmail("moderation@example.com");
        assertEquals(1, updated.getNWarning());
    }
}
