package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import model.Entity.FilmGenereBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.VisualizzaCatalogoServlet;

public class VisualizzaCatalogoServletIntegrationTest {

    private VisualizzaCatalogoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Setup del Database finto in memoria (inizializzato con init.sql)
        dataSource = DatabaseSetupForTest.getH2DataSource();
        FilmDAO filmDAO = new FilmDAO(dataSource);
        FilmGenereDAO filmGenereDAO = new FilmGenereDAO(dataSource);
        GenereDAO genereDAO = new GenereDAO(dataSource);
        catalogoService = new CatalogoService(filmDAO,  filmGenereDAO, genereDAO );

        // 2. Inizializzazione della Servlet
        servlet = new VisualizzaCatalogoServlet();
        servlet.init();
        
        // 3. Iniezione del Service tramite Reflection
        // N.B. Il nome del campo qui deve essere esattamente "CatalogoService" (con la C maiuscola) 
        // in base a come è dichiarato nella tua Servlet.
        Field serviceField = VisualizzaCatalogoServlet.class.getDeclaredField("CatalogoService");
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
    public void testVisualizzaCatalogoSuccess_Integration() throws Exception {
        // --- ACT ---
        // Poiché è una semplice visualizzazione, non servono parametri di request in ingresso
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Usiamo ArgumentCaptor per catturare la lista generale dei film salvata in sessione
        ArgumentCaptor<List<FilmBean>> filmsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("films"), filmsCaptor.capture());
        
        List<FilmBean> filmsRestituiti = filmsCaptor.getValue();
        
        // Assicuriamoci che il DB abbia restituito effettivamente il catalogo (in init.sql ci sono 11 film)
        assertNotNull(filmsRestituiti, "La lista dei film non dovrebbe essere nulla");
        assertFalse(filmsRestituiti.isEmpty(), "La lista dei film non dovrebbe essere vuota");
        
        // 2. Verifica del ciclo for per i Generi
        // Per ogni film restituito dalla query, la Servlet deve aver invocato il database per recuperare 
        // i generi e li deve aver salvati in sessione con la chiave: idFilm + "Generi"
        for (FilmBean film : filmsRestituiti) {
            ArgumentCaptor<List<FilmGenereBean>> generiCaptor = ArgumentCaptor.forClass((Class) List.class);
            String chiaveSessione = film.getIdFilm() + "Generi"; // NOTA: Presumo tu abbia il metodo getIdFilm() in FilmBean
            
            // Verifichiamo che il setAttribute sia avvenuto con la chiave esatta e catturiamo la lista risultante
            verify(session).setAttribute(eq(chiaveSessione), generiCaptor.capture());
            
            List<FilmGenereBean> generi = generiCaptor.getValue();
            assertNotNull(generi, "La lista dei generi per il film ID " + film.getIdFilm() + " è stata inizializzata dal Service");
        }

        // 3. Verifichiamo che la Servlet passi il controllo in avanti al componente view JSP corretto
        verify(dispatcher).forward(request, response);
    }
}