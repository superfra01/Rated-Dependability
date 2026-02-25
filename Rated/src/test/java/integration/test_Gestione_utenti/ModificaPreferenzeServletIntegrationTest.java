package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.ModificaPreferenzeServlet;

public class ModificaPreferenzeServletIntegrationTest {

    private ModificaPreferenzeServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private ProfileService profileService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente che modificherà le proprie preferenze
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('tester.prefs@example.com', 'PrefsTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Inseriamo i Generi nella tabella madre per evitare violazioni di Foreign Key
            String gen1 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Azione')";
            try(PreparedStatement ps = conn.prepareStatement(gen1)) { ps.executeUpdate(); }
            
            String gen2 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Commedia')";
            try(PreparedStatement ps = conn.prepareStatement(gen2)) { ps.executeUpdate(); }
            
            // Puliamo eventuali preferenze residue per questo utente per avere un test pulito
            String cleanPrefs = "DELETE FROM Preferenza WHERE email = 'tester.prefs@example.com'";
            try(PreparedStatement ps = conn.prepareStatement(cleanPrefs)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new ModificaPreferenzeServlet();
        servlet.init();
        
        // 4. Iniezione del Service (la variabile in ModificaPreferenzeServlet è "profileService")
        Field profField = ModificaPreferenzeServlet.class.getDeclaredField("profileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(request.getSession(true)).thenReturn(session); // Nel caso venga usato
    }

    @Test
    public void testModificaPreferenze_Success_Integration() throws Exception {
        // --- ARRANGE ---
        UtenteBean user = new UtenteBean();
        user.setEmail("tester.prefs@example.com");
        user.setUsername("PrefsTester");
        when(session.getAttribute("user")).thenReturn(user);

        // Parametri per l'aggiornamento
        when(request.getParameter("email")).thenReturn("tester.prefs@example.com");
        when(request.getParameterValues("selectedGenres")).thenReturn(new String[]{"Azione", "Commedia"});

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // 1. Verifica HTTP: Messaggio di successo in sessione e Redirect
        verify(session).setAttribute("messaggioSuccesso", "Preferenze aggiornate con successo!");
        verify(response).sendRedirect("profile?visitedUser=PrefsTester");

        // 2. Verifica Database: Assicuriamoci che i generi siano finiti nella tabella 'Preferenza'
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Preferenza WHERE email = ? AND (Nome_Genere = 'Azione' OR Nome_Genere = 'Commedia')")) {
            
            ps.setString(1, "tester.prefs@example.com");
            ResultSet rs = ps.executeQuery();
            rs.next();
            int countPreferenze = rs.getInt(1);
            
            assertEquals(2, countPreferenze, "Dovrebbero esserci esattamente 2 preferenze salvate per l'utente");
        }
    }

    @Test
    public void testModificaPreferenze_Unauthorized_IDOR() throws Exception {
        // --- ARRANGE ---
        // Utente loggato
        UtenteBean user = new UtenteBean();
        user.setEmail("tester.prefs@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // Tenta maliziosamente di modificare le preferenze di un'ALTRA email
        when(request.getParameter("email")).thenReturn("admin@example.com");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // Verifica HTTP: Deve restituire errore 403 Forbidden
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare le preferenze di questo utente.");
    }

    @Test
    public void testModificaPreferenze_NonLoggato_RedirectLogin() throws Exception {
        // --- ARRANGE ---
        when(session.getAttribute("user")).thenReturn(null);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // Se non c'è utente, deve rimandare al login
        verify(response).sendRedirect("login.jsp");
    }
    
    @Test
    public void testDoGet_RedirectToProfile() throws Exception {
        // --- ACT ---
        servlet.doGet(request, response);
        
        // --- ASSERT ---
        verify(response).sendRedirect("profile.jsp");
    }
}