package integration.test_Gestione_utenti;

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
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.SegnaComeVistoServlet;

public class SegnaComeVistoServletIntegrationTest {

    private SegnaComeVistoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private PrintWriter writer;
    private DataSource dataSource;
    
    private ProfileService profileService;
    private RecensioniService recensioniService;

    private static final String TEST_EMAIL = "visti.tester@example.com";

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES (?, 'VistiTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { 
                ps.setString(1, TEST_EMAIL);
                ps.executeUpdate(); 
            }

            // B. Film 2001: Da Aggiungere
            String film1Sql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (2001, 'Film Da Aggiungere', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(film1Sql)) { ps.executeUpdate(); }

            // C. Film 2002: Da Rimuovere (Visto, ma senza recensione)
            String film2Sql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (2002, 'Film Da Rimuovere', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(film2Sql)) { ps.executeUpdate(); }
            
            String visto2Sql = "MERGE INTO Visto (email, ID_Film) KEY(email, ID_Film) VALUES (?, 2002)";
            try(PreparedStatement ps = conn.prepareStatement(visto2Sql)) { 
                ps.setString(1, TEST_EMAIL);
                ps.executeUpdate(); 
            }

            // D. Film 2003: Conflitto (Visto E con Recensione)
            String film3Sql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (2003, 'Film Con Recensione', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(film3Sql)) { ps.executeUpdate(); }
            
            String visto3Sql = "MERGE INTO Visto (email, ID_Film) KEY(email, ID_Film) VALUES (?, 2003)";
            try(PreparedStatement ps = conn.prepareStatement(visto3Sql)) { 
                ps.setString(1, TEST_EMAIL);
                ps.executeUpdate(); 
            }
            
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES (?, 2003, 'Titolo', 'Contenuto', 5)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { 
                ps.setString(1, TEST_EMAIL);
                ps.executeUpdate(); 
            }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();
        recensioniService = new RecensioniService();

        // 3. Inizializzazione Servlet
        servlet = new SegnaComeVistoServlet();
        servlet.init();
        
        // 4. Iniezione multipla delle dipendenze
        Field profField = SegnaComeVistoServlet.class.getDeclaredField("profileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        Field recField = SegnaComeVistoServlet.class.getDeclaredField("recensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        writer = mock(PrintWriter.class);

        when(request.getSession()).thenReturn(session);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testAggiungiVisto_Success_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("filmId")).thenReturn("2001");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // Verifica profonda DB: Il film deve ora essere nella tabella Visto
        assertTrue(checkIfFilmIsVistoInDB(TEST_EMAIL, 2001), 
                "Il film 2001 deve essere stato inserito nella tabella 'Visto'");
    }

    @Test
    public void testRimuoviVisto_Success_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("filmId")).thenReturn("2002");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // Verifica profonda DB: Il film NON deve più essere nella tabella Visto
        assertFalse(checkIfFilmIsVistoInDB(TEST_EMAIL, 2002), 
                "Il film 2002 deve essere stato rimosso dalla tabella 'Visto'");
    }

    @Test
    public void testRimuoviVisto_Conflict_HasReview_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail(TEST_EMAIL);
        when(session.getAttribute("user")).thenReturn(user);

        // Passiamo l'ID del film che ha una recensione
        when(request.getParameter("filmId")).thenReturn("2003");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // Verifica Error HTTP 409
        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        verify(writer).write("Non puoi rimuovere il film dai 'Visti' perché hai scritto una recensione. Elimina prima la recensione.");

        // Verifica DB: Il film DEVE ancora essere presente nella tabella Visto
        assertTrue(checkIfFilmIsVistoInDB(TEST_EMAIL, 2003), 
                "Il film 2003 NON deve essere stato rimosso perché c'è una recensione associata");
    }

    @Test
    public void testSegnaComeVisto_UtenteNonLoggato_Unauthorized() throws Exception {
        when(session.getAttribute("user")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Utente non loggato.");
    }

    @Test
    public void testSegnaComeVisto_InvalidId_BadRequest() throws Exception {
        UtenteBean user = new UtenteBean();
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("filmId")).thenReturn("non_un_numero");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("ID Film non valido.");
    }

    @Test
    public void testDoGet_RedirectToCatalogo() throws Exception {
        servlet.doGet(request, response);
        verify(response).sendRedirect("catalogo.jsp");
    }

    /**
     * Utility method per verificare la presenza nella tabella Visto nel DB H2
     */
    private boolean checkIfFilmIsVistoInDB(String email, int idFilm) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Visto WHERE email = ? AND ID_Film = ?")) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true se trova il record
        }
    }
}