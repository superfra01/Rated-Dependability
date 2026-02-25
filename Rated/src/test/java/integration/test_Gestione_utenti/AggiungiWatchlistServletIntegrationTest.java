package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import sottosistemi.Gestione_Utenti.view.AggiungiWatchlistServlet;

public class AggiungiWatchlistServletIntegrationTest {

    private AggiungiWatchlistServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private PrintWriter writer;
    private DataSource dataSource;
    
    private ProfileService profileService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- SETUP ISOLATO PER TESTARE IL TOGGLE (AGGIUNGI/RIMUOVI) ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('watchlist.tester@example.com', 'WatchTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Film 1010 (NON presente nella watchlist di default)
            String film1Sql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (1010, 'Film Da Aggiungere', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(film1Sql)) { ps.executeUpdate(); }

            // C. Film 1011 (GIA' PRESENTE nella watchlist)
            String film2Sql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (1011, 'Film Da Rimuovere', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(film2Sql)) { ps.executeUpdate(); }
            
            String watchSql = "MERGE INTO Interesse (email, ID_Film, interesse) KEY(email, ID_Film) VALUES ('watchlist.tester@example.com', 1011, TRUE)";
            try(PreparedStatement ps = conn.prepareStatement(watchSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new AggiungiWatchlistServlet();
        servlet.init();
        
        // 4. Iniezione del Service (la variabile in AggiungiWatchlistServlet l'abbiamo chiamata profileService con la 'p' minuscola)
        Field profField = AggiungiWatchlistServlet.class.getDeclaredField("profileService");
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
    public void testAggiungiAllaWatchlist_Success_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail("watchlist.tester@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametro film 1010 (attualmente non in watchlist)
        when(request.getParameter("filmId")).thenReturn("1010");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(writer).write("Film aggiunto alla watchlist.");

        // Verifica profonda DB: Il film deve ora essere nella tabella Interesse
        assertTrue(checkIfInWatchlistDB("watchlist.tester@example.com", 1010), 
                "Il film 1010 deve essere stato inserito nel Database (Tabella Interesse)");
    }

    @Test
    public void testRimuoviDallaWatchlist_Success_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail("watchlist.tester@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametro film 1011 (già inserito in watchlist durante il setup)
        when(request.getParameter("filmId")).thenReturn("1011");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(writer).write("Film rimosso dalla watchlist.");

        // Verifica profonda DB: Il film NON deve più essere nella tabella Interesse
        assertFalse(checkIfInWatchlistDB("watchlist.tester@example.com", 1011), 
                "Il film 1011 deve essere stato eliminato dal Database (Tabella Interesse)");
    }

    @Test
    public void testWatchlist_UtenteNonLoggato_Unauthorized() throws Exception {
        // --- ARRANGE ---
        when(session.getAttribute("user")).thenReturn(null);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Devi effettuare il login per gestire la watchlist.");
    }

    @Test
    public void testWatchlist_ParametroScorretto_BadRequest() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("filmId")).thenReturn("idNonValido123");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("ID Film non valido.");
    }

    /**
     * Metodo di utility privato per controllare lo stato reale nel DB H2
     */
    private boolean checkIfInWatchlistDB(String email, int idFilm) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Interesse WHERE email = ? AND ID_Film = ?")) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true se trova il record, false se non lo trova
        }
    }
}