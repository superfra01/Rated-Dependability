package unit.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.HomePageServlet;

class HomePageServletTest {

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getLoadsAnonymousAndAuthenticatedHome() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        HomePageServlet servlet = new HomePageServlet();
        inject(servlet, "catalogoService", service);

        HttpSession anonymous = mock(HttpSession.class);
        HttpServletRequest anonymousRequest = mock(HttpServletRequest.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(anonymousRequest.getSession()).thenReturn(anonymous);
        when(anonymousRequest.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp")).thenReturn(dispatcher);
        servlet.doGet(anonymousRequest, mock(HttpServletResponse.class));
        verify(anonymous).setAttribute("filmConsigliati", null);

        UtenteBean user = new UtenteBean();
        HttpSession authenticated = mock(HttpSession.class);
        HttpServletRequest authenticatedRequest = mock(HttpServletRequest.class);
        when(authenticated.getAttribute("user")).thenReturn(user);
        when(authenticatedRequest.getSession()).thenReturn(authenticated);
        when(authenticatedRequest.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp")).thenReturn(dispatcher);
        when(service.getFilmCompatibili(user)).thenReturn(List.of(new FilmBean()));
        servlet.doGet(authenticatedRequest, mock(HttpServletResponse.class));
        verify(service).getFilmCompatibili(user);
    }

    @Test
    void getHandlesCommittedAndBrokenErrorResponses() throws Exception {
        HomePageServlet servlet = new HomePageServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenThrow(new IllegalStateException("session"));

        HttpServletResponse normal = mock(HttpServletResponse.class);
        servlet.doGet(request, normal);
        verify(normal).sendError(500, "Errore nel caricamento della HomePage.");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(500, "Errore nel caricamento della HomePage.");
        assertDoesNotThrow(() -> servlet.doGet(request, broken));
    }

    @Test
    void postHandlesDelegationFailureForEveryResponseState() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        HomePageServlet failed = spy(new HomePageServlet());
        HttpServletResponse normal = mock(HttpServletResponse.class);
        doThrow(new ServletException("forward")).when(failed).doGet(request, normal);
        failed.doPost(request, normal);
        verify(normal).sendError(500, "Errore interno durante l'elaborazione della richiesta.");

        HomePageServlet committedFailure = spy(new HomePageServlet());
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        doThrow(new IOException("forward")).when(committedFailure).doGet(request, committed);
        committedFailure.doPost(request, committed);

        HomePageServlet brokenFailure = spy(new HomePageServlet());
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("forward")).when(brokenFailure).doGet(request, broken);
        doThrow(new IOException("closed")).when(broken).sendError(500, "Errore interno durante l'elaborazione della richiesta.");
        assertDoesNotThrow(() -> brokenFailure.doPost(request, broken));
    }
}
