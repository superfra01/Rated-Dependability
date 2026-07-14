package unit.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.AggiungiFilmServlet;
import sottosistemi.Gestione_Catalogo.view.VisualizzaFilmServlet;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.NonInteressatoServlet;
import sottosistemi.Gestione_Utenti.view.ProfileServlet;
import sottosistemi.Gestione_Utenti.view.RegisterServlet;

class NonInteressatoServletTest {

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
        user.setEmail("user@example.com");
        user.setUsername("tester");
        user.setTipoUtente(role);
        return user;
    }

    private static HttpSession session(UtenteBean user) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user);
        return session;
    }

    private static HttpServletRequest filmRequest(HttpSession session, String id) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("idFilm")).thenReturn(id);
        return request;
    }

    private static HttpServletRequest registrationRequest(String username, String email, String password, String confirmation)
            throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirm_password")).thenReturn(confirmation);
        when(request.getPart("profile_icon")).thenReturn(null);
        return request;
    }

    @Test
    void nonInteressatoHandlesAnonymousAndInvalidIdentifiers() throws Exception {
        NonInteressatoServlet servlet = new NonInteressatoServlet();
        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession()).thenReturn(anonymousSession);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter anonymousWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(anonymousWriter);
        servlet.doPost(anonymous, response);
        verify(response).setContentType("text/plain");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(anonymousWriter).write("Devi effettuare il login.");

        for (String id : new String[] {null, "", "bad"}) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpSession authenticatedSession = session(user("UTENTE"));
            when(request.getSession()).thenReturn(authenticatedSession);
            when(request.getParameter("filmId")).thenReturn(id);
            HttpServletResponse invalid = mock(HttpServletResponse.class);
            PrintWriter invalidWriter = mock(PrintWriter.class);
            when(invalid.getWriter()).thenReturn(invalidWriter);
            servlet.doPost(request, invalid);
            verify(invalid).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            if (id == null || id.isEmpty()) {
                verify(invalidWriter).write("Impossibile identificare il film.");
            }
        }
    }

    @Test
    void nonInteressatoHandlesWriterServiceAndSendErrorFailures() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        NonInteressatoServlet servlet = new NonInteressatoServlet();
        inject(servlet, "profileService", profile);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession authenticatedSession = session(user("UTENTE"));
        when(request.getSession()).thenReturn(authenticatedSession);
        when(request.getParameter("filmId")).thenReturn("bad");
        HttpServletResponse writerFailure = mock(HttpServletResponse.class);
        when(writerFailure.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(request, writerFailure));

        when(request.getParameter("filmId")).thenReturn("7");
        servlet.doPost(request, mock(HttpServletResponse.class));
        verify(profile).ignoreFilm("user@example.com", 7);

        doThrow(new IllegalStateException("service")).when(profile).ignoreFilm(anyString(), anyInt());
        HttpServletResponse normalError = mock(HttpServletResponse.class);
        servlet.doPost(request, normalError);
        verify(normalError).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Si è verificato un errore durante l'elaborazione della richiesta.");

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(request, broken));

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession()).thenReturn(anonymousSession);
        HttpServletResponse brokenAnonymousWriter = mock(HttpServletResponse.class);
        when(brokenAnonymousWriter.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(anonymous, brokenAnonymousWriter));

        HttpServletRequest missingId = mock(HttpServletRequest.class);
        when(missingId.getSession()).thenReturn(authenticatedSession);
        when(missingId.getParameter("filmId")).thenReturn("");
        HttpServletResponse brokenMissingWriter = mock(HttpServletResponse.class);
        when(brokenMissingWriter.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(missingId, brokenMissingWriter));
    }

    @Test
    void nonInteressatoGetHandlesRedirectAndBothErrorBranches() throws Exception {
        NonInteressatoServlet servlet = new NonInteressatoServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/rated");
        HttpServletResponse ok = mock(HttpServletResponse.class);
        servlet.doGet(request, ok);
        verify(ok).sendRedirect("/rated/index.jsp");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(committed).sendRedirect(anyString());
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("redirect")).when(broken).sendRedirect(anyString());
        doThrow(new IOException("error")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, broken));

        HttpServletResponse normalError = mock(HttpServletResponse.class);
        doThrow(new IOException("redirect")).when(normalError).sendRedirect(anyString());
        servlet.doGet(request, normalError);
        verify(normalError).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Errore durante il reindirizzamento.");
    }

    private static HttpServletRequest addFilmRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession managerSession = session(user("GESTORE"));
        when(request.getSession(true)).thenReturn(managerSession);
        when(request.getParameter("annoFilm")).thenReturn("2024");
        when(request.getParameter("durataFilm")).thenReturn("120");
        when(request.getParameter("nomeFilm")).thenReturn("Film");
        when(request.getPart("locandinaFilm")).thenReturn(null);
        return request;
    }

}
