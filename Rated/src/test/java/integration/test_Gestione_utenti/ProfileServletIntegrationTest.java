package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import integration.DatabaseSetupForTest;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.ProfileServlet;

public class ProfileServletIntegrationTest {

    private ProfileServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    
    private ProfileService profileService;
    private RecensioniService recensioniService;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB finto H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente visitato
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('visited.user@example.com', 'VisitedTarget', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Film finto (ID = 7777)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (7777, 'Film Recensito', 2024, 120, 'Test Regista')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // C. Generi disponibili e relative associazioni
            String gen1 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Azione')";
            String gen2 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Fantascienza')";
            try(PreparedStatement ps = conn.prepareStatement(gen1)) { ps.executeUpdate(); }
            try(PreparedStatement ps = conn.prepareStatement(gen2)) { ps.executeUpdate(); }
            
            // D. Preferenza Utente ("Azione")
            String prefSql = "MERGE INTO Preferenza (email, Nome_Genere) KEY(email, Nome_Genere) VALUES ('visited.user@example.com', 'Azione')";
            try(PreparedStatement ps = conn.prepareStatement(prefSql)) { ps.executeUpdate(); }

            // E. Recensione dell'utente
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES ('visited.user@example.com', 7777, 'Ottimo', 'Bello!', 5)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();
        recensioniService = new RecensioniService();
        catalogoService = new CatalogoService();

        // 3. Inizializzazione Servlet
        servlet = new ProfileServlet();
        servlet.init();
        
        // 4. Iniezione multipla dei Service
        Field profField = ProfileServlet.class.getDeclaredField("profileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        Field recField = ProfileServlet.class.getDeclaredField("recensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);

        Field catField = ProfileServlet.class.getDeclaredField("catalogoService");
        catField.setAccessible(true);
        catField.set(servlet, catalogoService);

        // 5. Setup Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProfile_UserFound_Success_Integration() throws Exception {
        // --- ARRANGE ---
        // Cerchiamo l'utente che abbiamo creato nel Setup
        when(request.getParameter("visitedUser")).thenReturn("VisitedTarget");

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Verifica Dati Utente
        ArgumentCaptor<UtenteBean> userCaptor = ArgumentCaptor.forClass(UtenteBean.class);
        verify(session).setAttribute(eq("visitedUser"), userCaptor.capture());
        assertEquals("visited.user@example.com", userCaptor.getValue().getEmail(), "L'email dell'utente recuperato deve coincidere");

        // 2. Verifica Recensioni
        ArgumentCaptor<List<RecensioneBean>> recCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("recensioni"), recCaptor.capture());
        List<RecensioneBean> recensioni = recCaptor.getValue();
        assertFalse(recensioni.isEmpty(), "La lista delle recensioni non deve essere vuota");
        assertEquals(7777, recensioni.get(0).getIdFilm(), "La recensione deve riferirsi al film corretto");

        // 3. Verifica Film Estratti dalle Recensioni
        ArgumentCaptor<HashMap<Integer, FilmBean>> filmCaptor = ArgumentCaptor.forClass((Class) HashMap.class);
        verify(session).setAttribute(eq("films"), filmCaptor.capture());
        HashMap<Integer, FilmBean> filmMap = filmCaptor.getValue();
        assertTrue(filmMap.containsKey(7777), "La mappa dei film deve contenere il film recensito");

        // 4. Verifica Generi Globali
        ArgumentCaptor<List<String>> allGenresCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("allGenres"), allGenresCaptor.capture());
        assertTrue(allGenresCaptor.getValue().contains("Azione") && allGenresCaptor.getValue().contains("Fantascienza"), 
                "Tutti i generi di sistema devono essere caricati");

        // 5. Verifica Generi Preferiti dall'Utente
        ArgumentCaptor<List<String>> userGenresCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("userGenres"), userGenresCaptor.capture());
        assertTrue(userGenresCaptor.getValue().contains("Azione"), "Le preferenze personali dell'utente devono essere caricate");

        // 6. Verifica Forward alla JSP corretta
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testProfile_UserNotFound_BadRequest() throws Exception {
        // --- ARRANGE ---
        // Passiamo uno username che non esiste in H2
        when(request.getParameter("visitedUser")).thenReturn("UserInesistenteGhost");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // Verifica Error Code e stringa stampata in pagina
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("You can't access the profile page if visitedUser is not set");
        
        // Assicuriamoci che non venga interrogato in modo dispendioso il DB per caricare mappe/liste inesistenti
        verify(session, never()).setAttribute(eq("recensioni"), any());
        verify(dispatcher, never()).forward(request, response);
    }
}