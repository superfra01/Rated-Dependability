package unit.test_Gestione_utenti;

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
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.SegnaComeVistoServlet;

class SegnaComeVistoServletTest {

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static UtenteBean user() {
        UtenteBean user = new UtenteBean();
        user.setEmail("user@example.com");
        return user;
    }

    private static HttpServletRequest request(HttpSession session, String filmId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("filmId")).thenReturn(filmId);
        return request;
    }

    private static HttpServletResponse writableResponse() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        return response;
    }

    @Test
    void rejectsMissingSessionUserAndEveryInvalidFilmId() throws Exception {
        SegnaComeVistoServlet servlet = new SegnaComeVistoServlet();
        servlet.doPost(request(null, "1"), writableResponse());

        HttpSession anonymous = mock(HttpSession.class);
        servlet.doPost(request(anonymous, "1"), writableResponse());

        HttpSession logged = mock(HttpSession.class);
        when(logged.getAttribute("user")).thenReturn(user());
        for (String id : new String[] {null, "", "bad"}) {
            HttpServletResponse response = writableResponse();
            servlet.doPost(request(logged, id), response);
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Test
    void addsRemovesAndRejectsRemovalWhenReviewExists() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        RecensioniService reviews = mock(RecensioniService.class);
        SegnaComeVistoServlet servlet = new SegnaComeVistoServlet();
        inject(servlet, "profileService", profile);
        inject(servlet, "recensioniService", reviews);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());

        HttpServletResponse added = writableResponse();
        servlet.doPost(request(session, "7"), added);
        verify(profile).aggiungiFilmVisto("user@example.com", 7);
        verify(added).setContentType("text/plain");
        verify(added).setStatus(HttpServletResponse.SC_OK);

        when(profile.isFilmVisto("user@example.com", 8)).thenReturn(true);
        HttpServletResponse removed = writableResponse();
        servlet.doPost(request(session, "8"), removed);
        verify(profile).rimuoviFilmVisto("user@example.com", 8);

        when(profile.isFilmVisto("user@example.com", 9)).thenReturn(true);
        when(reviews.getRecensione(9, "user@example.com")).thenReturn(new RecensioneBean());
        HttpServletResponse conflict = writableResponse();
        servlet.doPost(request(session, "9"), conflict);
        verify(conflict).setStatus(HttpServletResponse.SC_CONFLICT);
    }

    @Test
    void safeErrorsAndCriticalErrorsRespectResponseState() throws Exception {
        SegnaComeVistoServlet servlet = new SegnaComeVistoServlet();
        HttpServletResponse brokenWriter = mock(HttpServletResponse.class);
        when(brokenWriter.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(request(null, "1"), brokenWriter));

        HttpServletResponse committedSafe = mock(HttpServletResponse.class);
        when(committedSafe.isCommitted()).thenReturn(true);
        servlet.doPost(request(null, "1"), committedSafe);
        verify(committedSafe, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ProfileService profile = mock(ProfileService.class);
        SegnaComeVistoServlet failed = new SegnaComeVistoServlet();
        inject(failed, "profileService", profile);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        when(profile.isFilmVisto("user@example.com", 7)).thenThrow(new IllegalStateException("db"));

        HttpServletResponse normal = writableResponse();
        failed.doPost(request(session, "7"), normal);
        verify(normal).sendError(anyInt(), anyString());

        HttpServletResponse committed = writableResponse();
        when(committed.isCommitted()).thenReturn(true);
        failed.doPost(request(session, "7"), committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = writableResponse();
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> failed.doPost(request(session, "7"), broken));
    }

    @Test
    void getCoversEmptyPresentAndBrokenContextRedirects() throws Exception {
        SegnaComeVistoServlet servlet = new SegnaComeVistoServlet();
        for (String contextPath : new String[] {null, "", "/Rated"}) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getContextPath()).thenReturn(contextPath);
            servlet.doGet(request, mock(HttpServletResponse.class));
        }

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, broken));
    }
}
