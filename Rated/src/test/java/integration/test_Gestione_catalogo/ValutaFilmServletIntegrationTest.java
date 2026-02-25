package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.view.ValutaFilmServlet;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;

public class ValutaFilmServletIntegrationTest {

    private ValutaFilmServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    
    private RecensioniService recensioniService;
    private ProfileService profileService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database finto in memoria
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- INIZIO FIX: ISOLAMENTO DEL TEST ---
        // Inseriamo un utente e un film finti, univoci per questo test, 
        // così nessun altro test potrà averli cancellati per sbaglio.
        try (Connection conn = dataSource.getConnection()) {
            
            // Creiamo un Utente di test
            String insertUser = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, "tester.valutazione@example.com");
                ps.setString(2, "TesterValutazione");
                ps.setString(3, "passwordFinta123");
                ps.setString(4, "RECENSORE");
                ps.executeUpdate();
            }

            // Creiamo un Film di test (ID = 999)
            String insertFilm = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista, Trama, Valutazione) KEY(ID_Film) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertFilm)) {
                ps.setInt(1, 999);
                ps.setString(2, "Film per Test Valutazione");
                ps.setInt(3, 2024);
                ps.setInt(4, 120);
                ps.setString(5, "Regista Test");
                ps.setString(6, "Trama Test");
                ps.setInt(7, 3); // Valutazione base valida
                ps.executeUpdate();
            }
        }
        // --- FINE FIX ---

        recensioniService = new RecensioniService(); 
        profileService = new ProfileService();

        // 3. Inizializzazione della Servlet
        servlet = new ValutaFilmServlet();
        servlet.init();
        
        // 4. Iniezione del RecensioniService tramite Reflection
        Field recensioniField = ValutaFilmServlet.class.getDeclaredField("RecensioniService");
        recensioniField.setAccessible(true);
        recensioniField.set(servlet, recensioniService);
        
        // 5. Iniezione del ProfileService tramite Reflection
        Field profileField = ValutaFilmServlet.class.getDeclaredField("ProfileService");
        profileField.setAccessible(true);
        profileField.set(servlet, profileService);

        // 6. Inizializzazione dei Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testValutaFilmSuccess_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Usiamo l'utente inserito dinamicamente nel Setup
        UtenteBean user = new UtenteBean();
        user.setEmail("tester.valutazione@example.com");
        user.setTipoUtente("RECENSORE");
        when(session.getAttribute("user")).thenReturn(user);

        // Usiamo il film inserito dinamicamente nel Setup (ID = 999)
        int idFilm = 999;
        when(request.getParameter("idFilm")).thenReturn(String.valueOf(idFilm));
        when(request.getParameter("titolo")).thenReturn("Test Recensione Integrata 100% Isolata");
        when(request.getParameter("recensione")).thenReturn("Contenuto della recensione per il DB.");
        when(request.getParameter("valutazione")).thenReturn("4");
        
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica HTTP: assicuriamoci che abbia effettuato il redirect
        verify(response).sendRedirect("/Rated/film?idFilm=" + idFilm);

        // 2. Verifica Database "Visto": assicuriamoci che il ProfileService abbia registrato il film come "Visto"
        // Ora questo assert funzionerà sempre al 100% perché i dati di partenza sono intatti
        boolean isFilmVisto = profileService.isFilmVisto(user.getEmail(), idFilm);
        assertTrue(isFilmVisto, "Il film deve essere stato impostato come 'Visto' nel profilo utente");
    }
}