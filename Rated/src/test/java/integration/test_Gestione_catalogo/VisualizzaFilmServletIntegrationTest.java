package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import model.Entity.FilmGenereBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.VisualizzaFilmServlet;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;

public class VisualizzaFilmServletIntegrationTest {

    private VisualizzaFilmServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    
    private CatalogoService catalogoService;
    private RecensioniService recensioniService;
    private ProfileService profileService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database finto H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente finto
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('vis.tester@example.com', 'VisTester', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { ps.executeUpdate(); }

            // B. Film finto (ID = 888)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (888, 'Integration Test Movie', 2024, 120, 'Test Director')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // --- FIX: Inseriamo il Genere prima di usarlo come Foreign Key ---
            String insertGenere = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Azione')";
            try(PreparedStatement ps = conn.prepareStatement(insertGenere)) { ps.executeUpdate(); }
            // -----------------------------------------------------------------

            // C. Inseriamo l'associazione Genere-Film
            String genSql = "MERGE INTO Film_Genere (ID_Film, Nome_Genere) KEY(ID_Film, Nome_Genere) VALUES (888, 'Azione')";
            try(PreparedStatement ps = conn.prepareStatement(genSql)) { ps.executeUpdate(); }

            // D. Inseriamo una Recensione da parte del nostro utente
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione) KEY(email, ID_Film) VALUES ('vis.tester@example.com', 888, 'Test Titolo', 'Test Recensione', 5)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }

            // E. Settiamo il film come Visto e in Watchlist per l'utente
            String visSql = "MERGE INTO Visto (email, ID_Film) KEY(email, ID_Film) VALUES ('vis.tester@example.com', 888)";
            try(PreparedStatement ps = conn.prepareStatement(visSql)) { ps.executeUpdate(); }

            String watchSql = "MERGE INTO Interesse (email, ID_Film, interesse) KEY(email, ID_Film) VALUES ('vis.tester@example.com', 888, TRUE)";
            try(PreparedStatement ps = conn.prepareStatement(watchSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        catalogoService = new CatalogoService();
        recensioniService = new RecensioniService();
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new VisualizzaFilmServlet();
        servlet.init();
        
        // 4. Iniezione multipla delle dipendenze
        Field catField = VisualizzaFilmServlet.class.getDeclaredField("catalogoService");
        catField.setAccessible(true);
        catField.set(servlet, catalogoService);
        
        Field recField = VisualizzaFilmServlet.class.getDeclaredField("recensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);
        
        Field profField = VisualizzaFilmServlet.class.getDeclaredField("profileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/film.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVisualizzaFilm_UserLogged_Success_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("idFilm")).thenReturn("888");
        
        UtenteBean user = new UtenteBean();
        user.setEmail("vis.tester@example.com");
        user.setUsername("VisTester");
        when(session.getAttribute("user")).thenReturn(user);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        ArgumentCaptor<FilmBean> filmCaptor = ArgumentCaptor.forClass(FilmBean.class);
        verify(session).setAttribute(eq("film"), filmCaptor.capture());
        assertEquals("Integration Test Movie", filmCaptor.getValue().getNome());

        ArgumentCaptor<List<FilmGenereBean>> generiCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("Generi"), generiCaptor.capture());
        assertFalse(generiCaptor.getValue().isEmpty());

        ArgumentCaptor<List<RecensioneBean>> recCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("recensioni"), recCaptor.capture());
        assertFalse(recCaptor.getValue().isEmpty());
        verify(session).setAttribute(eq("users"), any(HashMap.class));

        verify(session).setAttribute("watched", true);
        verify(session).setAttribute("inwatchlist", true);

        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testVisualizzaFilm_GuestUser_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("idFilm")).thenReturn("888");
        when(session.getAttribute("user")).thenReturn(null);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        verify(session).setAttribute(eq("film"), any(FilmBean.class));
        verify(session).setAttribute(eq("recensioni"), any(List.class));

        verify(session).setAttribute("watched", false);
        verify(session).setAttribute("inwatchlist", false);

        verify(session, never()).setAttribute(eq("valutazioni"), any());

        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testVisualizzaFilm_InvalidIdParam() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("idFilm")).thenReturn("idNonValido");

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        verify(response).sendRedirect("catalogo.jsp");
        verify(session, never()).setAttribute(eq("film"), any());
    }
}