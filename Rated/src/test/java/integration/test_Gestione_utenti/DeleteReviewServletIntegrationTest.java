package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import sottosistemi.Gestione_Utenti.view.DeleteReviewServlet;

public class DeleteReviewServletIntegrationTest {

    private DeleteReviewServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private RecensioniService recensioniService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione Database in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente proprietario della recensione
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('author.delete@example.com', 'AuthorDelete', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Film finto (ID = 987)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (987, 'Film Per Eliminazione', 2024, 120, 'Test Regista')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // C. La Recensione da eliminare
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES ('author.delete@example.com', 987, 'Titolo Test', 'Testo recensione da cancellare', 3)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        recensioniService = new RecensioniService();

        // 3. Inizializzazione Servlet
        servlet = new DeleteReviewServlet();
        servlet.init();
        
        // 4. Iniezione della dipendenza tramite Reflection (Nome del campo "RecensioniService")
        Field recField = DeleteReviewServlet.class.getDeclaredField("RecensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testDeleteReview_Success_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Utente loggato in sessione (colui che vuole cancellare la sua recensione)
        UtenteBean user = new UtenteBean();
        user.setEmail("author.delete@example.com");
        user.setUsername("AuthorDelete");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametro ID del film della recensione da cancellare
        when(request.getParameter("DeleteFilmID")).thenReturn("987");
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica Redirect alla pagina profilo corretta (con lo username passato in querystring)
        verify(response).sendRedirect("/Rated/profile?visitedUser=AuthorDelete");

        // 2. Verifica Database: Controlliamo che il database NON abbia più la recensione salvata
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Recensione WHERE email = ? AND ID_Film = ?")) {
            
            ps.setString(1, "author.delete@example.com");
            ps.setInt(2, 987);
            
            ResultSet rs = ps.executeQuery();
            rs.next();
            int conteggioRecensioni = rs.getInt(1);
            
            // L'asserzione verifica che la conta sia tornata a 0
            assertEquals(0, conteggioRecensioni, "La recensione dell'utente per il film 987 deve essere stata eliminata dal Database");
        }
    }
}