package integration.test_Gestione_recensioni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
import sottosistemi.Gestione_Recensioni.view.VoteReviewServlet;

public class VoteReviewServletIntegrationTest {

    private VoteReviewServlet servlet;
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
            
            // A. Utente Votante (Colui che mette il Like)
            String voterSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('voter@example.com', 'VoterUser', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(voterSql)) { ps.executeUpdate(); }

            // B. Utente Recensore (L'autore della recensione)
            String reviewerSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('reviewer@example.com', 'ReviewerUser', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(reviewerSql)) { ps.executeUpdate(); }

            // C. Film finto (ID = 333)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (333, 'Film Da Valutare', 2024, 120, 'Regista Test')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // D. La Recensione bersaglio
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES ('reviewer@example.com', 333, 'Titolo', 'Contenuto bello', 4)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        recensioniService = new RecensioniService();

        // 3. Inizializzazione Servlet
        servlet = new VoteReviewServlet();
        servlet.init();
        
        // 4. Iniezione del Service tramite Reflection (Nome variabile "RecensioniService" con la maiuscola)
        Field recField = VoteReviewServlet.class.getDeclaredField("RecensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testVoteReview_Success_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo l'utente loggato che mette il Like
        UtenteBean user = new UtenteBean();
        user.setEmail("voter@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametri inviati (es. tramite AJAX) alla servlet
        when(request.getParameter("idFilm")).thenReturn("333");
        when(request.getParameter("emailRecensore")).thenReturn("reviewer@example.com");
        when(request.getParameter("valutazione")).thenReturn("true"); // true = Like, false = Dislike

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica DB: Controlliamo che il database abbia registrato correttamente il Like (Tabella Valutazione)
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT Like_Dislike FROM Valutazione WHERE email = ? AND email_Recensore = ? AND ID_Film = ?")) {
            
            ps.setString(1, "voter@example.com");
            ps.setString(2, "reviewer@example.com");
            ps.setInt(3, 333);
            
            ResultSet rs = ps.executeQuery();
            
            // Assicuriamoci che esista la riga
            assertTrue(rs.next(), "Il record della valutazione deve esistere nel database");
            
            // Assicuriamoci che il valore sia true (Like) e non false
            boolean votoInserito = rs.getBoolean("Like_Dislike");
            assertEquals(true, votoInserito, "La valutazione inserita dovrebbe essere 'true' (Like)");
        }
    }
}