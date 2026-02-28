package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.NonInteressatoServlet;

public class NonInteressatoServletIntegrationTest {

    private NonInteressatoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private PrintWriter writer;
    private DataSource dataSource;
    
    private ProfileService profileService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente che effettua l'azione
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('ignore.tester@example.com', 'IgnoreTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Film finto (ID = 2020)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (2020, 'Film Non Interessante', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }
            
            // Pulizia preventiva (se ci fosse un'esecuzione precedente sporca)
            String cleanSql = "DELETE FROM Interesse WHERE email = 'ignore.tester@example.com'";
            try(PreparedStatement ps = conn.prepareStatement(cleanSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new NonInteressatoServlet();
        servlet.init();
        
        // 4. Iniezione del Service (la variabile in NonInteressatoServlet è "profileService")
        Field profField = NonInteressatoServlet.class.getDeclaredField("profileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        writer = mock(PrintWriter.class);

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testNonInteressato_Success_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail("ignore.tester@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametro inviato dal frontend
        when(request.getParameter("filmId")).thenReturn("2020");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // 1. Verifica HTTP (status 200 OK)
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // 2. Verifica Database: controlliamo che sia stata creata una tupla con "interesse" a FALSE
        assertTrue(checkIfFilmIsIgnoredInDB("ignore.tester@example.com", 2020), 
                   "Il database dovrebbe avere un record in 'Interesse' con valore false per questo film e utente");
    }

    @Test
    public void testNonInteressato_UtenteNonLoggato_Unauthorized() throws Exception {
        // --- ARRANGE ---
        when(session.getAttribute("user")).thenReturn(null);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Devi effettuare il login.");
    }

    @Test
    public void testNonInteressato_InvalidId_BadRequest() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        when(session.getAttribute("user")).thenReturn(user);

        // ID sporco che fa fallire Integer.parseInt()
        when(request.getParameter("filmId")).thenReturn("abc");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("ID Film non valido.");
    }
    
    @Test
    public void testDoGet_RedirectToHomePage() throws Exception {
        // --- ACT ---
        servlet.doGet(request, response);
        
        // --- ASSERT ---
        verify(response).sendRedirect(org.mockito.ArgumentMatchers.anyString());
    }

    /**
     * Utility method per verificare lo stato dell'interesse nel DB
     */
    private boolean checkIfFilmIsIgnoredInDB(String email, int idFilm) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT interesse FROM Interesse WHERE email = ? AND ID_Film = ?")) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isInterested = rs.getBoolean("interesse");
                // Se interesse è FALSE, allora l'utente lo ha effettivamente "ignorato"
                return !isInterested; 
            }
            return false; // Se non c'è il record, il test fallisce
        }
    }
}