package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import sottosistemi.Gestione_Catalogo.view.RimuoviFilmServlet;

public class RimuoviFilmServletIntegrationTest {

    private RimuoviFilmServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DataSource dataSource;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database finto in memoria (caricato con init.sql)
        dataSource = DatabaseSetupForTest.getH2DataSource();
        FilmDAO filmDAO = new FilmDAO(dataSource);
        FilmGenereDAO filmGenereDAO = new FilmGenereDAO(dataSource);
        GenereDAO genereDAO = new GenereDAO(dataSource);
        catalogoService = new CatalogoService(filmDAO,  filmGenereDAO, genereDAO );

        // 2. Inizializzazione della Servlet
        servlet = new RimuoviFilmServlet();
        servlet.init();
        
        // 3. Iniezione del Service tramite Reflection
        // N.B: Il nome del campo usato nella servlet è "CatalogoService" con la C maiuscola.
        Field serviceField = RimuoviFilmServlet.class.getDeclaredField("catalogoService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, catalogoService);

        // 4. Inizializzazione dei Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testRimuoviFilmSuccess_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo un utente loggato con i permessi di "GESTORE"
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("GESTORE");
        when(session.getAttribute("user")).thenReturn(user);

        // Nel tuo init.sql, il film con ID = 2 è "The Matrix". Testiamo la sua rimozione.
        int idDaCancellare = 2;
        when(request.getParameter("idFilm")).thenReturn(String.valueOf(idDaCancellare));
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifichiamo che la Servlet abbia reindirizzato l'utente al catalogo aggiornato
        verify(response).sendRedirect("/Rated/catalogo");

        // 2. Verifica nel Database (Integration): Assicuriamoci che il film sia stato effettivamente eliminato
        // Usiamo il service per cercare "The Matrix" e ci assicuriamo che l'ID 2 non sia più tra i risultati
        List<FilmBean> risultatiRicerca = catalogoService.ricercaFilm("The Matrix");
        
        boolean filmAncoraPresente = risultatiRicerca.stream()
                .anyMatch(f -> f.getIdFilm() == idDaCancellare); // Assumendo che il getter sia getId()

        assertFalse(filmAncoraPresente, "Il film con ID 2 (The Matrix) dovrebbe essere stato eliminato dal Database");
    }

    @Test
    public void testRimuoviFilmUnauthorized_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo un utente loggato con permessi insufficienti ("RECENSORE")
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("RECENSORE");
        when(session.getAttribute("user")).thenReturn(user);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // Verifichiamo che la Servlet rifiuti la richiesta impostando lo stato 401 Unauthorized
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Non hai i permessi per effettuare la seguente operazione");
        
        // Assicuriamoci che la Servlet si fermi immediatamente senza tentare di leggere l'ID o fare redirect
        verify(request, never()).getParameter("idFilm");
        verify(response, never()).sendRedirect(anyString());
    }
}