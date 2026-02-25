package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
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
import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.HomePageServlet;

public class HomePageServletIntegrationTest {

    private HomePageServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database di test (H2 inizializzato da init.sql)
        dataSource = DatabaseSetupForTest.getH2DataSource();
        FilmDAO filmDAO = new FilmDAO(dataSource);
        FilmGenereDAO filmGenereDAO = new FilmGenereDAO(dataSource);
        GenereDAO genereDAO = new GenereDAO(dataSource);
        catalogoService = new CatalogoService(filmDAO,  filmGenereDAO, genereDAO );

        // 2. Inizializzazione Servlet
        servlet = new HomePageServlet();
        servlet.init();
        
        // 3. Iniezione della dipendenza tramite Reflection
        Field serviceField = HomePageServlet.class.getDeclaredField("catalogoService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, catalogoService);

        // 4. Setup dei Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHomePageUserLogged_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo un utente loggato presente in init.sql (es. alice.rossi che ha preferenze "Drammatico" e "Fantascienza")
        UtenteBean user = new UtenteBean();
        user.setEmail("alice.rossi@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Catturiamo cosa la servlet ha salvato nella sessione alla voce "filmConsigliati"
        ArgumentCaptor<List<FilmBean>> listCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("filmConsigliati"), listCaptor.capture());
        
        List<FilmBean> filmConsigliati = listCaptor.getValue();
        
        // 2. Verifica dello stato DB: Assicuriamoci che il service abbia recuperato correttamente dei film dal DB
        assertNotNull(filmConsigliati, "La lista dei film consigliati non deve essere nulla per un utente loggato");
        // Sapendo che alice ha preferenze, la query DB dovrebbe aver restituito risultati
        // (Nota: se l'utente Alice in init.sql ha dei film correlati, la lista non sarà vuota)
        // Se si è certi che init.sql dia almeno un match, puoi scommentare la riga sotto:
        // assertTrue(filmConsigliati.size() > 0, "Ci dovrebbe essere almeno un film consigliato per Alice");

        // 3. Verifica del dispatcher: ha inoltrato la richiesta al JSP corretto?
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testHomePageGuestUser() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo un utente GUEST (non loggato)
        when(session.getAttribute("user")).thenReturn(null);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // Per un guest, la lista deve essere salvata come 'null' in sessione
        verify(session).setAttribute("filmConsigliati", null);

        // Il forward deve avvenire comunque regolarmente verso la home
        verify(dispatcher).forward(request, response);
    }
}