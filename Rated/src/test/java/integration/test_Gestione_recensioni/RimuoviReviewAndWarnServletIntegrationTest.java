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
import sottosistemi.Gestione_Recensioni.view.RimuoviReviewAndWarnServlet;
import sottosistemi.Gestione_Utenti.service.ModerationService;

public class RimuoviReviewAndWarnServletIntegrationTest {

    private RimuoviReviewAndWarnServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private RecensioniService recensioniService;
    private ModerationService moderationService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione Database in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente Moderatore (esegue l'azione)
            String modSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente, N_Warning) KEY(email) VALUES ('moderator.del@example.com', 'ModRimuovi', 'pwd', 'MODERATORE', 0)";
            try(PreparedStatement ps = conn.prepareStatement(modSql)) { ps.executeUpdate(); }

            // B. Utente Recensore Bersaglio (Parte con 0 warning)
            String targetSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente, N_Warning) KEY(email) VALUES ('target.warn@example.com', 'TargetWarn', 'pwd', 'RECENSORE', 0)";
            try(PreparedStatement ps = conn.prepareStatement(targetSql)) { ps.executeUpdate(); }

            // C. Film finto (ID = 666)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (666, 'Film Da Cancellare', 2024, 120, 'Regista Test')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // D. La Recensione incriminata
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES ('target.warn@example.com', 666, 'Titolo Spam', 'Contenuto da rimuovere', 1)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        recensioniService = new RecensioniService();
        moderationService = new ModerationService();

        // 3. Inizializzazione Servlet
        servlet = new RimuoviReviewAndWarnServlet();
        servlet.init();
        
        // 4. Iniezione multipla delle dipendenze (Uso esatto del nome delle variabili dichiarate nella tua Servlet)
        Field recField = RimuoviReviewAndWarnServlet.class.getDeclaredField("RecensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);
        
        Field modField = RimuoviReviewAndWarnServlet.class.getDeclaredField("ModerationService");
        modField.setAccessible(true);
        modField.set(servlet, moderationService);

        // 5. Setup Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testRimuoviReviewAndWarn_Moderatore_Success_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo l'accesso da parte di un Moderatore
        UtenteBean user = new UtenteBean();
        user.setEmail("moderator.del@example.com");
        user.setTipoUtente("MODERATORE");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametri inviati dal Moderatore
        when(request.getParameter("ReviewUserEmail")).thenReturn("target.warn@example.com");
        when(request.getParameter("idFilm")).thenReturn("666");
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica HTTP: deve aver reindirizzato la pagina alla dashboard
        verify(response).sendRedirect("/Rated/moderator");

        // 2. Verifica Database - La recensione DEVE essere stata eliminata
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Recensione WHERE email = ? AND ID_Film = ?")) {
            ps.setString(1, "target.warn@example.com");
            ps.setInt(2, 666);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int conteggioRecensioni = rs.getInt(1);
            assertEquals(0, conteggioRecensioni, "La recensione dovrebbe essere stata eliminata dal database");
        }
        
        // 3. Verifica Database - L'utente target DEVE aver ricevuto un warning (da 0 a 1)
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT N_Warning FROM Utente_Registrato WHERE email = ?")) {
            ps.setString(1, "target.warn@example.com");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int numeroWarning = rs.getInt("N_Warning");
                assertEquals(1, numeroWarning, "L'utente target deve avere esattamente 1 warning dopo l'operazione");
            }
        }
    }

    @Test
    public void testRimuoviReviewAndWarn_NonModeratore_Unauthorized() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo l'accesso da parte di un utente standard che tenta un'azione malevola
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("RECENSORE");
        when(session.getAttribute("user")).thenReturn(user);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // HTTP 401 Unauthorized
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Non hai i permessi per effettuare la seguente operazione");
        
        // Sicurezza: Verifichiamo che la Servlet si fermi immediatamente
        verify(request, never()).getParameter("ReviewUserEmail");
        verify(request, never()).getParameter("idFilm");
    }
}