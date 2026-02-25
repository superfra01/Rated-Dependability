package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import model.Entity.FilmGenereBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.ViewUserFilmsServlet;

public class ViewUserFilmsServletIntegrationTest {

    private ViewUserFilmsServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    
    private ProfileService profileService;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB finto H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente Bersaglio
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('target.films@example.com', 'TargetUserFilms', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Inseriamo i film (3001: Visto, 3002: Watchlist)
            String f1 = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (3001, 'Film Visto', 2024, 120)";
            String f2 = "MERGE INTO Film (ID_Film, Nome, Anno, Durata) KEY(ID_Film) VALUES (3002, 'Film Watchlist', 2024, 120)";
            try(PreparedStatement ps = conn.prepareStatement(f1)) { ps.executeUpdate(); }
            try(PreparedStatement ps = conn.prepareStatement(f2)) { ps.executeUpdate(); }

            // C. Associazioni Utente-Film
            String vistoSql = "MERGE INTO Visto (email, ID_Film) KEY(email, ID_Film) VALUES ('target.films@example.com', 3001)";
            String watchSql = "MERGE INTO Interesse (email, ID_Film, interesse) KEY(email, ID_Film) VALUES ('target.films@example.com', 3002, TRUE)";
            try(PreparedStatement ps = conn.prepareStatement(vistoSql)) { ps.executeUpdate(); }
            try(PreparedStatement ps = conn.prepareStatement(watchSql)) { ps.executeUpdate(); }

            // D. Inseriamo i generi e le associazioni per testare populateGenres()
            String gen1 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Horror')";
            String gen2 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Thriller')";
            try(PreparedStatement ps = conn.prepareStatement(gen1)) { ps.executeUpdate(); }
            try(PreparedStatement ps = conn.prepareStatement(gen2)) { ps.executeUpdate(); }
            
            String fg1 = "MERGE INTO Film_Genere (ID_Film, Nome_Genere) KEY(ID_Film, Nome_Genere) VALUES (3001, 'Horror')";
            String fg2 = "MERGE INTO Film_Genere (ID_Film, Nome_Genere) KEY(ID_Film, Nome_Genere) VALUES (3002, 'Thriller')";
            try(PreparedStatement ps = conn.prepareStatement(fg1)) { ps.executeUpdate(); }
            try(PreparedStatement ps = conn.prepareStatement(fg2)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();
        catalogoService = new CatalogoService();

        // 3. Inizializzazione Servlet
        servlet = new ViewUserFilmsServlet();
        servlet.init();
        
        // 4. Iniezione dei Service tramite Reflection
        Field profField = ViewUserFilmsServlet.class.getDeclaredField("profileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        Field catField = ViewUserFilmsServlet.class.getDeclaredField("catalogoService");
        catField.setAccessible(true);
        catField.set(servlet, catalogoService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/userFilms.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testViewUserFilms_Success_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("username")).thenReturn("TargetUserFilms");

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Verifica Dati Utente
        ArgumentCaptor<UtenteBean> userCaptor = ArgumentCaptor.forClass(UtenteBean.class);
        verify(session).setAttribute(eq("visitedUser"), userCaptor.capture());
        assertEquals("target.films@example.com", userCaptor.getValue().getEmail(), "Deve recuperare correttamente l'utente");

        // 2. Verifica Liste Film
        ArgumentCaptor<List<FilmBean>> watchedCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<List<FilmBean>> watchlistCaptor = ArgumentCaptor.forClass((Class) List.class);
        
        verify(session).setAttribute(eq("watchedList"), watchedCaptor.capture());
        verify(session).setAttribute(eq("watchlist"), watchlistCaptor.capture());
        
        List<FilmBean> watchedFilms = watchedCaptor.getValue();
        List<FilmBean> watchlistFilms = watchlistCaptor.getValue();
        
        assertFalse(watchedFilms.isEmpty(), "La lista dei film visti non deve essere vuota");
        assertEquals(3001, watchedFilms.get(0).getIdFilm(), "Il film visto deve essere il 3001");
        
        assertFalse(watchlistFilms.isEmpty(), "La watchlist non deve essere vuota");
        assertEquals(3002, watchlistFilms.get(0).getIdFilm(), "Il film in watchlist deve essere il 3002");

        // 3. Verifica Popolamento Generi
        ArgumentCaptor<List<FilmGenereBean>> generiCaptor = ArgumentCaptor.forClass((Class) List.class);
        // Il metodo populateGenres salva in sessione con chiave "IDGeneri" (es. "3001Generi")
        verify(session).setAttribute(eq("3001Generi"), generiCaptor.capture());
        assertNotNull(generiCaptor.getValue(), "I generi del film 3001 devono essere caricati in sessione");
        
        verify(session).setAttribute(eq("3002Generi"), any(List.class));

        // 4. Verifica Forward
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testViewUserFilms_UserNotFound_Error() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("username")).thenReturn("GhostUserInesistente");

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Utente non trovato");
    }
}