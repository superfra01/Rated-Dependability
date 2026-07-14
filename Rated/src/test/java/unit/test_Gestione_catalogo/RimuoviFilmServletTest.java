package unit.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.RimuoviFilmServlet;

class RimuoviFilmServletTest {

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static UtenteBean user(String role) {
        UtenteBean user = new UtenteBean();
        user.setTipoUtente(role);
        return user;
    }

    private static HttpServletRequest request(UtenteBean user, String id) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("idFilm")).thenReturn(id);
        when(request.getContextPath()).thenReturn("/Rated");
        return request;
    }

    @Test
    void rejectsMissingSessionWrongRoleAndInvalidIds() throws Exception {
        RimuoviFilmServlet servlet = new RimuoviFilmServlet();
        HttpServletRequest noSession = mock(HttpServletRequest.class);
        when(noSession.getSession(true)).thenReturn(null);
        HttpServletResponse noSessionResponse = mock(HttpServletResponse.class);
        when(noSessionResponse.getWriter()).thenReturn(mock(PrintWriter.class));
        servlet.doPost(noSession, noSessionResponse);

        HttpServletResponse broken = mock(HttpServletResponse.class);
        when(broken.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(request(user("UTENTE"), "7"), broken));

        for (String id : new String[] {null, ""}) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            servlet.doPost(request(user("GESTORE"), id), response);
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Test
    void removesFilmWithBothContextPathAndResponseStates() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        RimuoviFilmServlet servlet = new RimuoviFilmServlet();
        inject(servlet, "catalogoService", service);

        HttpServletRequest normalRequest = request(user("GESTORE"), "7");
        HttpServletResponse normal = mock(HttpServletResponse.class);
        servlet.doPost(normalRequest, normal);
        verify(service).removeFilm(7);
        verify(normal).sendRedirect("/Rated/catalogo");

        HttpServletRequest nullContext = request(user("GESTORE"), "8");
        when(nullContext.getContextPath()).thenReturn(null);
        HttpServletResponse nullContextResponse = mock(HttpServletResponse.class);
        servlet.doPost(nullContext, nullContextResponse);
        verify(nullContextResponse).sendRedirect("/catalogo");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request(user("GESTORE"), "9"), committed);
        verify(committed, never()).sendRedirect(anyString());

        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doPost(request(user("GESTORE"), "10"), redirectFailure));
    }

    @Test
    void handlesOuterFailureForEveryResponseState() throws Exception {
        RimuoviFilmServlet servlet = new RimuoviFilmServlet();
        HttpServletRequest invalid = request(user("GESTORE"), "bad");

        HttpServletResponse normal = mock(HttpServletResponse.class);
        servlet.doPost(invalid, normal);
        verify(normal).sendError(500, "Si è verificato un errore critico imprevisto.");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(invalid, committed);

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(invalid, broken));
    }

    @Test
    void getHandlesRedirectFailure() throws Exception {
        RimuoviFilmServlet servlet = new RimuoviFilmServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/Rated");
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(response).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }
}
