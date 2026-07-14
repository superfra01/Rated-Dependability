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

class ProfileServletTest {

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

    @Test
    void profileValidatesMissingAndUnknownUsersIncludingWriterFailure() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ProfileServlet servlet = new ProfileServlet();
        inject(servlet, "profileService", profile);
        HttpServletRequest missing = mock(HttpServletRequest.class);
        when(missing.getSession(true)).thenReturn(mock(HttpSession.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        servlet.doGet(missing, response);
        verify(writer).write("Parametro 'visitedUser' mancante.");

        HttpServletRequest unknown = mock(HttpServletRequest.class);
        when(unknown.getSession(true)).thenReturn(mock(HttpSession.class));
        when(unknown.getParameter("visitedUser")).thenReturn("missing");
        servlet.doGet(unknown, response);
        verify(writer).write("You can't access the profile page if visitedUser is not set");

        HttpServletResponse broken = mock(HttpServletResponse.class);
        when(broken.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doGet(missing, broken));
    }

    @Test
    void profileUsesFallbackCollectionsAndForwards() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        RecensioniService reviews = mock(RecensioniService.class);
        CatalogoService catalog = mock(CatalogoService.class);
        ProfileServlet servlet = new ProfileServlet();
        inject(servlet, "profileService", profile);
        inject(servlet, "recensioniService", reviews);
        inject(servlet, "catalogoService", catalog);
        UtenteBean visited = user("UTENTE");
        when(profile.findByUsername("tester")).thenReturn(visited);
        when(reviews.FindRecensioni(visited.getEmail())).thenReturn(null);
        when(catalog.getFilms(null)).thenReturn(null);
        when(catalog.getAllGeneri()).thenReturn(null);
        when(profile.getPreferenze(visited.getEmail())).thenReturn(null);
        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("visitedUser")).thenReturn("tester");
        when(request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp")).thenReturn(mock(RequestDispatcher.class));

        servlet.doGet(request, mock(HttpServletResponse.class));

        verify(session).setAttribute(eq("recensioni"), eq(Collections.emptyList()));
        verify(session).setAttribute(eq("films"), any(HashMap.class));
        verify(session).setAttribute(eq("allGenres"), eq(Collections.emptyList()));
        verify(session).setAttribute(eq("userGenres"), eq(Collections.emptyList()));
    }

    @Test
    void profileIsolatesBothRelatedDataFailures() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        RecensioniService reviews = mock(RecensioniService.class);
        CatalogoService catalog = mock(CatalogoService.class);
        ProfileServlet servlet = new ProfileServlet();
        inject(servlet, "profileService", profile);
        inject(servlet, "recensioniService", reviews);
        inject(servlet, "catalogoService", catalog);
        when(profile.findByUsername("tester")).thenReturn(user("UTENTE"));
        when(reviews.FindRecensioni(anyString())).thenThrow(new IllegalStateException("reviews"));
        when(catalog.getAllGeneri()).thenThrow(new IllegalStateException("genres"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession failedSession = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(failedSession);
        when(request.getParameter("visitedUser")).thenReturn("tester");
        when(request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp")).thenReturn(mock(RequestDispatcher.class));

        assertDoesNotThrow(() -> servlet.doGet(request, mock(HttpServletResponse.class)));
        verify(failedSession).setAttribute(eq("recensioni"), eq(Collections.emptyList()));
        verify(failedSession).setAttribute(eq("films"), any(HashMap.class));
        verify(failedSession).setAttribute(eq("allGenres"), eq(Collections.emptyList()));
        verify(failedSession).setAttribute(eq("userGenres"), eq(Collections.emptyList()));
    }

    @Test
    void profileHandlesForwardGlobalAndPostDelegationFailures() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ProfileServlet servlet = new ProfileServlet();
        inject(servlet, "profileService", profile);
        when(profile.findByUsername("tester")).thenReturn(user("UTENTE"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));
        when(request.getParameter("visitedUser")).thenReturn("tester");
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp")).thenReturn(dispatcher);
        doThrow(new IOException("forward")).when(dispatcher).forward(any(), any());
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doGet(request, response);
        verify(response).sendError(500, "Errore interno durante il caricamento della vista profilo.");

        HttpServletRequest global = mock(HttpServletRequest.class);
        when(global.getSession(true)).thenThrow(new IllegalStateException("session"));
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doGet(global, broken));

        ProfileServlet spy = spy(new ProfileServlet());
        doThrow(new ServletException("delegation")).when(spy).doGet(request, response);
        spy.doPost(request, response);
        verify(response).sendError(500, "Errore interno durante il caricamento del profilo.");
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

    @Test
    void profileCoversEmptyParameterAndCommittedErrorHelpers() throws Exception {
        ProfileServlet servlet = new ProfileServlet();

        HttpServletRequest empty = mock(HttpServletRequest.class);
        when(empty.getSession(true)).thenReturn(mock(HttpSession.class));
        when(empty.getParameter("visitedUser")).thenReturn("");
        servlet.doGet(empty, mock(HttpServletResponse.class));

        HttpServletResponse committedSafe = mock(HttpServletResponse.class);
        when(committedSafe.isCommitted()).thenReturn(true);
        servlet.doGet(empty, committedSafe);
        verify(committedSafe, never()).setStatus(anyInt());

        HttpServletRequest failed = mock(HttpServletRequest.class);
        when(failed.getSession(true)).thenThrow(new IllegalStateException("session"));
        HttpServletResponse committedCritical = mock(HttpServletResponse.class);
        when(committedCritical.isCommitted()).thenReturn(true);
        servlet.doGet(failed, committedCritical);
        verify(committedCritical, never()).sendError(anyInt(), anyString());
    }
}
