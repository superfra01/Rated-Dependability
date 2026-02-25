package integration.test_Gestione_recensioni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import sottosistemi.Gestione_Recensioni.view.ApproveReviewServlet;

public class ApproveReviewServletIntegrationTest {

    private ApproveReviewServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private RecensioniService recensioniService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database finto H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente Recensore (Colui che subisce il report)
            String revSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('reviewer.test@example.com', 'RevTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(revSql)) { ps.executeUpdate(); }

            // B. Utente Segnalatore (Colui che fa il report)
            String repSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('reporter.test@example.com', 'RepTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(repSql)) { ps.executeUpdate(); }

            // C. Film finto (ID = 777)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (777, 'Film per Report', 2024, 120, 'Regista')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // D. Inseriamo la Recensione
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES ('reviewer.test@example.com', 777, 'Titolo', 'Testo molto offensivo', 1)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }

            // E. Inseriamo la SEGNALAZIONE (Report) sulla recensione
            String reportSql = "MERGE INTO Report (email, email_Recensore, ID_Film) KEY(email, email_Recensore, ID_Film) VALUES ('reporter.test@example.com', 'reviewer.test@example.com', 777)";
            try(PreparedStatement ps = conn.prepareStatement(reportSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        recensioniService = new RecensioniService();

        // 3. Inizializzazione Servlet
        servlet = new ApproveReviewServlet();
        servlet.init();
        
        // 4. Iniezione del Service tramite Reflection (Nome variabile "RecensioniService" con la maiuscola)
        Field recField = ApproveReviewServlet.class.getDeclaredField("RecensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testApproveReview_Moderatore_Success_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Utente in sessione con ruolo corretto (MODERATORE)
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("MODERATORE");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametri per eliminare il report isolato che abbiamo creato
        when(request.getParameter("ReviewUserEmail")).thenReturn("reviewer.test@example.com");
        when(request.getParameter("idFilm")).thenReturn("777");
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica Redirect
        verify(response).sendRedirect("/Rated/moderator");

        // 2. Verifica DB: Il report DEVE essere stato cancellato dalla tabella Report
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Report WHERE email_Recensore = ? AND ID_Film = ?")) {
            ps.setString(1, "reviewer.test@example.com");
            ps.setInt(2, 777);
            
            ResultSet rs = ps.executeQuery();
            rs.next();
            int reportCount = rs.getInt(1);
            
            assertEquals(0, reportCount, "Il report dovrebbe essere stato eliminato (approvando la recensione) dal Database");
        }
    }

    @Test
    public void testApproveReview_NonModeratore_Unauthorized() throws Exception {
        // --- ARRANGE ---
        
        // Utente senza permessi
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("RECENSORE"); // O GESTORE
        when(session.getAttribute("user")).thenReturn(user);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica HTTP Status 401
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Non hai i permessi per effettuare la seguente operazione");
        
        // 2. Verifica che non abbia provato a leggere i parametri vitali
        verify(request, never()).getParameter("ReviewUserEmail");
        verify(request, never()).getParameter("idFilm");
    }
}