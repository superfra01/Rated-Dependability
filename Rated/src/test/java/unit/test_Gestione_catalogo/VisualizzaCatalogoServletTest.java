package unit.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import integration.DatabaseSetupForTest;
import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.RicercaCatalogoServlet;
import sottosistemi.Gestione_Catalogo.view.ValutaFilmServlet;
import sottosistemi.Gestione_Catalogo.view.VisualizzaCatalogoServlet;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Recensioni.view.ReportReviewServlet;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.DeleteReviewServlet;

class VisualizzaCatalogoServletTest {

    private static final String ERROR_CONTENT_TYPE = "text/plain;charset=UTF-8";

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static UtenteBean user() {
        UtenteBean user = new UtenteBean();
        user.setEmail("user@example.com");
        user.setUsername("test-user");
        return user;
    }

    @Test
    void visualizzaCatalogoCoversNullFilmGenresAndForward() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        VisualizzaCatalogoServlet servlet = new VisualizzaCatalogoServlet();
        inject(servlet, "catalogoService", service);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp")).thenReturn(dispatcher);

        when(service.getFilms()).thenReturn(null);
        servlet.doGet(request, response);
        verify(session).setAttribute("films", Collections.emptyList());

        FilmBean film = new FilmBean();
        film.setIdFilm(5);
        when(service.getFilms()).thenReturn(Arrays.asList(null, film));
        when(service.getGeneri(5)).thenReturn(null);
        servlet.doGet(request, response);
        verify(session).setAttribute("5Generi", Collections.emptyList());
        verify(dispatcher, times(2)).forward(request, response);
    }

    @Test
    void visualizzaCatalogoHandlesServiceGenreForwardAndGlobalFailures() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        VisualizzaCatalogoServlet servlet = new VisualizzaCatalogoServlet();
        inject(servlet, "catalogoService", service);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);

        when(service.getFilms()).thenThrow(new IllegalStateException("database"));
        servlet.doGet(request, response);
        verify(response).setContentType(ERROR_CONTENT_TYPE);
        verify(response).sendError(500, "Impossibile recuperare il catalogo dei film.");

        FilmBean film = new FilmBean();
        film.setIdFilm(6);
        doReturn(Collections.singletonList(film)).when(service).getFilms();
        when(service.getGeneri(6)).thenThrow(new IllegalStateException("genres"));
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(session).setAttribute("6Generi", Collections.emptyList());

        doThrow(new ServletException("forward")).when(dispatcher).forward(request, response);
        servlet.doGet(request, response);
        verify(response).sendError(500, "Errore interno durante il caricamento della pagina catalogo.");

        HttpServletRequest globalFailure = mock(HttpServletRequest.class);
        when(globalFailure.getSession(true)).thenThrow(new IllegalStateException("session"));
        servlet.doGet(globalFailure, response);
        verify(response).sendError(500, "Si è verificato un errore critico nel sistema.");
    }

    @Test
    void visualizzaCatalogoCriticalErrorHandlesCommittedAndIoFailure() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        VisualizzaCatalogoServlet servlet = new VisualizzaCatalogoServlet();
        inject(servlet, "catalogoService", service);
        when(service.getFilms()).thenThrow(new IllegalStateException("database"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse ioFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(ioFailure)
                .sendError(500, "Impossibile recuperare il catalogo dei film.");
        assertDoesNotThrow(() -> servlet.doGet(request, ioFailure));
    }

    @Test
    void catalogPostDelegationMethodIsCovered() throws Exception {
        VisualizzaCatalogoServlet servlet = org.mockito.Mockito.spy(new VisualizzaCatalogoServlet());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        org.mockito.Mockito.doNothing().when(servlet).doGet(request, response);

        servlet.doPost(request, response);

        verify(servlet).doGet(request, response);
    }
}
