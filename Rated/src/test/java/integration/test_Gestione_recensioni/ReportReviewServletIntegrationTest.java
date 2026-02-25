package integration.test_Gestione_recensioni;

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
import sottosistemi.Gestione_Recensioni.view.ReportReviewServlet;

public class ReportReviewServletIntegrationTest {

    private ReportReviewServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private RecensioniService recensioniService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente Segnalatore (Colui che è loggato e preme "Segnala")
            String repSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('reporter.user@example.com', 'ReporterUser', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(repSql)) { ps.executeUpdate(); }

            // B. Utente Recensore (Colui che ha scritto la recensione)
            String revSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('target.reviewer@example.com', 'TargetReviewer', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(revSql)) { ps.executeUpdate(); }

            // C. Film finto (ID = 444)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (444, 'Film Da Segnalare', 2024, 120, 'Regista Test')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // D. La Recensione scritta dal Recensore
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione, N_Reports) KEY(email, ID_Film) VALUES ('target.reviewer@example.com', 444, 'Titolo da segnalare', 'Testo della recensione...', 2, 0)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        recensioniService = new RecensioniService();

        // 3. Inizializzazione Servlet
        servlet = new ReportReviewServlet();
        servlet.init();
        
        // 4. Iniezione del Service tramite Reflection (Nome variabile "RecensioniService")
        Field recField = ReportReviewServlet.class.getDeclaredField("RecensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testReportReview_Success_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Utente loggato che effettua la segnalazione (Reporter)
        UtenteBean user = new UtenteBean();
        user.setEmail("reporter.user@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametri inviati dal form HTML per segnalare la recensione di "TargetReviewer"
        when(request.getParameter("reviewerEmail")).thenReturn("target.reviewer@example.com");
        when(request.getParameter("idFilm")).thenReturn("444");
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica Redirect
        verify(response).sendRedirect("/Rated/film?idFilm=444");

        // 2. Verifica DB - Integrità della Tabella Report
        // Assicuriamoci che nel database sia stata creata la tupla che lega il segnalatore alla recensione segnalata.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Report WHERE email = ? AND email_Recensore = ? AND ID_Film = ?")) {
            
            ps.setString(1, "reporter.user@example.com");
            ps.setString(2, "target.reviewer@example.com");
            ps.setInt(3, 444);
            
            ResultSet rs = ps.executeQuery();
            rs.next();
            int reportCount = rs.getInt(1);
            
            assertEquals(1, reportCount, "Dovrebbe esserci esattamente 1 segnalazione (Report) registrata nel Database per questa recensione");
        }
        
        // 3. (Opzionale) Verifica DB - Aggiornamento contatore N_Reports nella recensione
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT N_Reports FROM Recensione WHERE email = ? AND ID_Film = ?")) {
            
            ps.setString(1, "target.reviewer@example.com");
            ps.setInt(2, 444);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int nReports = rs.getInt("N_Reports");
                // La recensione partiva da 0 reports (vedi Setup), ora deve essere a 1
                assertEquals(1, nReports, "Il contatore N_Reports della recensione dovrebbe essere stato incrementato a 1");
            }
        }
    }
}