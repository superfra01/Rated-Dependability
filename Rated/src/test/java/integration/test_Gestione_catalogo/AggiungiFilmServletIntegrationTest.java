package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.AggiungiFilmServlet;

public class AggiungiFilmServletIntegrationTest {

    private AggiungiFilmServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private Part filePart;
    private DataSource dataSource;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database di test (H2)
        dataSource = DatabaseSetupForTest.getH2DataSource();
        FilmDAO filmDAO = new FilmDAO(dataSource);
        FilmGenereDAO filmGenereDAO = new FilmGenereDAO(dataSource);
        GenereDAO genereDAO = new GenereDAO(dataSource);
        catalogoService = new CatalogoService(filmDAO,  filmGenereDAO, genereDAO );

        // 2. Inizializzazione Servlet
        servlet = new AggiungiFilmServlet();
        servlet.init();
        
        // 3. Iniezione della dipendenza tramite Reflection
        // Questo permette di sovrascrivere il service creato nel metodo init() della Servlet
        // per fargli usare la connessione al database di Test invece di quella di produzione.
        Field serviceField = AggiungiFilmServlet.class.getDeclaredField("CatalogoService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, catalogoService);

        // 4. Mock degli oggetti della Servlet API
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        filePart = mock(Part.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testAggiungiFilmSuccess_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Mock dell'utente in sessione con privilegi "GESTORE"
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("GESTORE");
        when(session.getAttribute("user")).thenReturn(user);

        // Mock dei parametri del form nella request
        when(request.getParameter("annoFilm")).thenReturn("2023");
        when(request.getParameter("attoriFilm")).thenReturn("Attore 1, Attore 2");
        when(request.getParameter("durataFilm")).thenReturn("120");
        when(request.getParameterValues("generiFilm")).thenReturn(new String[]{"Azione", "Drammatico"});
        when(request.getParameter("nomeFilm")).thenReturn("Film Integration Test");
        when(request.getParameter("registaFilm")).thenReturn("Regista di Test");
        when(request.getParameter("tramaFilm")).thenReturn("Trama di test per verificare il passaggio nel DB.");

        // Mock del caricamento del file "locandinaFilm"
        when(request.getPart("locandinaFilm")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(150L);
        InputStream is = new ByteArrayInputStream("contenuto finto locandina".getBytes());
        when(filePart.getInputStream()).thenReturn(is);

        // Mock del percorso di context
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica che la request sia andata a buon fine e sia scaturito il Redirect
        verify(response).sendRedirect("/Rated/catalogo");

        // 2. Verifica dello stato del DB (Integration Verification vera e propria)
        // Leggiamo tutti i film e ci assicuriamo che "Film Integration Test" esista
        List<FilmBean> catalogoAttuale = catalogoService.ricercaFilm("Film Integration Test");
        boolean filmTrovato = catalogoAttuale.stream()
            .anyMatch(f -> "Film Integration Test".equals(f.getNome()));
            
        assertTrue(filmTrovato, "Il film dovrebbe essere stato inserito correttamente nel Database.");
    }

    @Test
    public void testAggiungiFilmUnauthorized() throws Exception {
        // --- ARRANGE ---
        
        // Mock dell'utente con privilegi "RECENSORE" (non GESTORE)
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("RECENSORE");
        when(session.getAttribute("user")).thenReturn(user);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // Verifichiamo che la Servlet rifiuti la richiesta impostando lo status "SC_UNAUTHORIZED" (401)
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Non hai i permessi per effettuare la seguente operazione");
    }
}