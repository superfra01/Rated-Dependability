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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;

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
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Recensioni.view.ApproveReviewServlet;
import sottosistemi.Gestione_Recensioni.view.ReportedReviewServlet;
import sottosistemi.Gestione_Recensioni.view.RimuoviReviewAndWarnServlet;
import sottosistemi.Gestione_Recensioni.view.VoteReviewServlet;
import sottosistemi.Gestione_Utenti.service.ModerationService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.AggiungiWatchlistServlet;
import sottosistemi.Gestione_Utenti.view.ModificaPreferenzeServlet;
import sottosistemi.Gestione_Utenti.view.ViewUserFilmsServlet;

class ViewUserFilmsServletTest {

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
        UtenteBean value = new UtenteBean();
        value.setEmail("user@example.com");
        value.setUsername("tester");
        value.setTipoUtente(role);
        return value;
    }

    private static HttpSession session(UtenteBean user) {
        HttpSession value = mock(HttpSession.class);
        when(value.getAttribute("user")).thenReturn(user);
        return value;
    }

    private static HttpServletResponse writableResponse() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        return response;
    }

    private static HttpServletRequest moderatorRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession moderatorSession = session(user("MODERATORE"));
        when(request.getSession(true)).thenReturn(moderatorSession);
        when(request.getParameter("ReviewUserEmail")).thenReturn("author@example.com");
        when(request.getParameter("idFilm")).thenReturn("7");
        return request;
    }

    @Test
    void viewUserFilmsCoversNotFoundNullListsGenresAndCachedGenre() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        CatalogoService catalog = mock(CatalogoService.class);
        ViewUserFilmsServlet servlet = new ViewUserFilmsServlet();
        inject(servlet, "profileService", profile);
        inject(servlet, "catalogoService", catalog);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("username")).thenReturn("tester");

        HttpServletResponse notFoundBroken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(notFoundBroken).sendError(eq(404), anyString());
        servlet.doGet(request, notFoundBroken);

        when(profile.findByUsername("tester")).thenReturn(user("UTENTE"));
        when(profile.retrieveWatchedFilms("tester")).thenReturn(null);
        when(profile.retrieveWatchlist("tester")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/jsp/userFilms.jsp")).thenReturn(mock(RequestDispatcher.class));
        servlet.doGet(request, mock(HttpServletResponse.class));

        FilmBean film = new FilmBean();
        film.setIdFilm(7);
        doReturn(java.util.Arrays.asList(null, film)).when(profile).retrieveWatchedFilms("tester");
        doReturn(Collections.singletonList(film)).when(profile).retrieveWatchlist("tester");
        when(session.getAttribute("7Generi")).thenReturn(null, "cached");
        when(catalog.getGeneri(7)).thenReturn(null);
        servlet.doGet(request, mock(HttpServletResponse.class));
    }

    @Test
    void viewUserFilmsCoversGlobalPostAndResponseFailureBranches() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ViewUserFilmsServlet servlet = new ViewUserFilmsServlet();
        inject(servlet, "profileService", profile);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(profile.findByUsername(any())).thenThrow(new IllegalStateException("service"));
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doGet(request, broken);

        ViewUserFilmsServlet spy = spy(new ViewUserFilmsServlet());
        doThrow(new IOException("delegation")).when(spy).doGet(request, broken);
        spy.doPost(request, broken);
    }

    @Test
    void viewUserFilmsDoesNotWriteNotFoundErrorAfterCommit() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ViewUserFilmsServlet servlet = new ViewUserFilmsServlet();
        inject(servlet, "profileService", profile);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);

        servlet.doGet(request, committed);

        verify(committed, never()).sendError(anyInt(), anyString());
    }

    @Test
    void viewUserFilmsCoversCommittedGetAndPostFailures() throws Exception {
        ProfileService failedProfile = mock(ProfileService.class);
        when(failedProfile.findByUsername(anyString())).thenThrow(new IllegalStateException("service"));
        ViewUserFilmsServlet servlet = new ViewUserFilmsServlet();
        inject(servlet, "profileService", failedProfile);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("username")).thenReturn("tester");
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        HttpServletResponse committedGet = mock(HttpServletResponse.class);
        when(committedGet.isCommitted()).thenReturn(true);
        servlet.doGet(request, committedGet);
        verify(committedGet, never()).sendError(anyInt(), anyString());

        HttpServletResponse normalGetError = mock(HttpServletResponse.class);
        servlet.doGet(request, normalGetError);
        verify(normalGetError).sendError(anyInt(), anyString());

        ViewUserFilmsServlet failingPost = new ViewUserFilmsServlet() {
            @Override
            public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
                throw new ServletException("delegation");
            }
        };
        HttpServletResponse committedPost = mock(HttpServletResponse.class);
        when(committedPost.isCommitted()).thenReturn(true);
        failingPost.doPost(request, committedPost);
        verify(committedPost, never()).sendError(anyInt(), anyString());
    }

    @Test
    void viewUserFilmsPostDelegationIsObservableInSuccessAndFailure() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        ViewUserFilmsServlet normal = spy(new ViewUserFilmsServlet());
        HttpServletResponse normalResponse = mock(HttpServletResponse.class);
        org.mockito.Mockito.doNothing().when(normal).doGet(request, normalResponse);
        normal.doPost(request, normalResponse);
        verify(normal).doGet(request, normalResponse);

        ViewUserFilmsServlet failing = new ViewUserFilmsServlet() {
            @Override
            public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
                throw new ServletException("delegation");
            }
        };
        HttpServletResponse error = mock(HttpServletResponse.class);
        failing.doPost(request, error);
        verify(error).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Errore interno durante l'elaborazione della richiesta.");
    }
}
