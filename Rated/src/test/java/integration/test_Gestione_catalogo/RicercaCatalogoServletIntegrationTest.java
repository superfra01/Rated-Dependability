package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.RicercaCatalogoServlet;

public class RicercaCatalogoServletIntegrationTest {

    private RicercaCatalogoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. RIPRISTINA IL DATABASE PRIMA DI ESEGUIRE IL TEST!
        DatabaseSetupForTest.resetDatabase();

        // 2. Setup del Database finto in memoria
        dataSource = DatabaseSetupForTest.getH2DataSource();
        FilmDAO filmDAO = new FilmDAO(dataSource);
        FilmGenereDAO filmGenereDAO = new FilmGenereDAO(dataSource);
        GenereDAO genereDAO = new GenereDAO(dataSource);
        catalogoService = new CatalogoService(filmDAO,  filmGenereDAO, genereDAO );

        // 2. Inizializzazione della Servlet
        servlet = new RicercaCatalogoServlet();
        servlet.init();
        
        // 3. Iniezione del Service tramite Reflection
        // N.B. Il nome del campo qui deve essere esattamente "CatalogoService" (con la C maiuscola) 
        // perché così è definito all'interno della tua RicercaCatalogoServlet.
        Field serviceField = RicercaCatalogoServlet.class.getDeclaredField("catalogoService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, catalogoService);

        // 4. Inizializzazione Mock dell'API Servlet
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRicercaFilmTrovato_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo l'input dell'utente dalla barra di ricerca. 
        // "Inception" è sicuramente presente nel tuo init.sql
        String parametroRicerca = "Inception";
        when(request.getParameter("filmCercato")).thenReturn(parametroRicerca);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Usiamo ArgumentCaptor per "catturare" la lista che la Servlet ha appena salvato nella Sessione
        ArgumentCaptor<List<FilmBean>> listCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("films"), listCaptor.capture());
        
        List<FilmBean> filmsRestituiti = listCaptor.getValue();
        
        // 2. Assicuriamoci che il DB abbia restituito effettivamente il film cercato
        assertNotNull(filmsRestituiti, "La lista dei film non dovrebbe essere nulla");
        assertFalse(filmsRestituiti.isEmpty(), "La lista dei film non dovrebbe essere vuota cercando 'Inception'");
        
        boolean filmTrovato = filmsRestituiti.stream()
                .anyMatch(f -> f.getNome().toLowerCase().contains(parametroRicerca.toLowerCase()));
        assertTrue(filmTrovato, "Il film 'Inception' deve essere presente all'interno della lista restituita");

        // 3. Verifichiamo che la Servlet passi il controllo alla JSP corretta
        verify(dispatcher).forward(request, response);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRicercaFilmNonTrovato_Integration() throws Exception {
        // --- ARRANGE ---
        
        // L'utente cerca una stringa priva di corrispondenze nel DB
        String parametroRicerca = "StringaCheNonCorrispondeANessunFilm123";
        when(request.getParameter("filmCercato")).thenReturn(parametroRicerca);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // Catturiamo la lista passata alla sessione
        ArgumentCaptor<List<FilmBean>> listCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("films"), listCaptor.capture());
        
        List<FilmBean> filmsRestituiti = listCaptor.getValue();
        
        // Verifichiamo il comportamento corretto: la lista deve esistere (non null) ma essere vuota
        assertNotNull(filmsRestituiti, "La lista dovrebbe essere inizializzata anche se non ci sono risultati");
        assertTrue(filmsRestituiti.isEmpty(), "La lista dei film deve essere vuota");

        // Il forward deve avvenire comunque per mostrare all'utente l'interfaccia (con 0 risultati)
        verify(dispatcher).forward(request, response);
    }
}