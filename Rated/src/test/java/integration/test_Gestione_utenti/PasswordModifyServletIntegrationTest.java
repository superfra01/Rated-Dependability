package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.PasswordModifyServlet;
import utilities.PasswordUtility;

public class PasswordModifyServletIntegrationTest {

    private PasswordModifyServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private ProfileService profileService;

    // Dati per isolare il test
    private static final String TEST_EMAIL = "test.pwd@example.com";
    private static final String OLD_PASSWORD = "OldPassword123!";
    private static final String NEW_VALID_PASSWORD = "NewPassword99!";
    private static final String NEW_INVALID_PASSWORD = "weak";

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // Generiamo l'hash della vecchia password (adatta la chiamata se la tua utility ha un nome metodo diverso)
            String oldHash = PasswordUtility.hashPassword(OLD_PASSWORD);

            // Inseriamo l'utente nel database H2
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES (?, 'PwdChanger', ?, 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { 
                ps.setString(1, TEST_EMAIL);
                ps.setString(2, oldHash);
                ps.executeUpdate(); 
            }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new PasswordModifyServlet();
        servlet.init();
        
        // 4. Iniezione del Service (la variabile in PasswordModifyServlet ha la "P" maiuscola: "ProfileService")
        Field profField = PasswordModifyServlet.class.getDeclaredField("ProfileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        // 5. Mock HTTP e comportamento Sessione
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/Rated");

        // Simula il comportamento reale di una HttpSession (fondamentale perché la servlet fa getAttribute subito dopo il setAttribute)
        Map<String, Object> sessionAttributes = new HashMap<>();
        
        doAnswer(invocation -> {
            sessionAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());

        when(session.getAttribute(anyString())).thenAnswer(invocation -> {
            return sessionAttributes.get(invocation.getArgument(0));
        });
    }

    @Test
    public void testPasswordModify_Success_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("email")).thenReturn(TEST_EMAIL);
        when(request.getParameter("password")).thenReturn(NEW_VALID_PASSWORD); // Password che passa FieldValidator

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica HTTP: deve aver aggiornato la sessione e reindirizzato usando l'username "PwdChanger"
        verify(session).setAttribute(eq("user"), any(UtenteBean.class));
        verify(response).sendRedirect("/Rated/profile?visitedUser=PwdChanger");

        // 2. Verifica Database: L'hash nel database DEVE essere cambiato e coincidere col nuovo hash
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT Password FROM Utente_Registrato WHERE email = ?")) {
            
            ps.setString(1, TEST_EMAIL);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String dbHash = rs.getString("Password");
            
            String expectedNewHash = PasswordUtility.hashPassword(NEW_VALID_PASSWORD);
            String oldHash = PasswordUtility.hashPassword(OLD_PASSWORD);
            
            // Assicuriamoci che non sia rimasta la vecchia password
            assertNotEquals(oldHash, dbHash, "La password nel DB deve essere stata aggiornata");
            // Assicuriamoci che l'hash salvato corrisponda alla nuova password valida
            assertEquals(expectedNewHash, dbHash, "Il nuovo hash nel DB deve corrispondere alla nuova password");
        }
    }

    @Test
    public void testPasswordModify_InvalidFormat_NoAction() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("email")).thenReturn(TEST_EMAIL);
        when(request.getParameter("password")).thenReturn(NEW_INVALID_PASSWORD); // "weak" (fallirà FieldValidator)

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Il FieldValidator dovrebbe aver bloccato l'esecuzione: nessun salvataggio in sessione, nessun redirect.
        verify(session, never()).setAttribute(anyString(), any());
        verify(response).sendRedirect(org.mockito.ArgumentMatchers.contains("invalidPassword"));

        // 2. Verifica Database: La password deve essere rimasta quella originale
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT Password FROM Utente_Registrato WHERE email = ?")) {
            
            ps.setString(1, TEST_EMAIL);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String dbHash = rs.getString("Password");
            
            String expectedOldHash = PasswordUtility.hashPassword(OLD_PASSWORD);
            
            assertEquals(expectedOldHash, dbHash, "L'hash nel DB NON deve cambiare se il formato della password era scorretto");
        }
    }
}