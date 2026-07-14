package unit.test_Gestione_catalogo;

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

class VisualizzaFilmServletTest {

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
    void visualizzaFilmRedirectsForMissingInvalidAndUnknownFilm() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        inject(servlet, "catalogoService", catalog);

        for (String id : new String[] {null, "", "bad", "8"}) {
            HttpServletRequest request = filmRequest(mock(HttpSession.class), id);
            HttpServletResponse response = mock(HttpServletResponse.class);
            servlet.doGet(request, response);
            verify(response).sendRedirect("catalogo.jsp");
        }
        verify(catalog).getFilm(8);
    }

    @Test
    void visualizzaFilmUsesEmptyFallbacksForMissingRelatedData() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        RecensioniService reviews = mock(RecensioniService.class);
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        inject(servlet, "catalogoService", catalog);
        inject(servlet, "recensioniService", reviews);
        FilmBean film = new FilmBean();
        film.setIdFilm(7);
        when(catalog.getFilm(7)).thenReturn(film);
        when(catalog.getGeneri(7)).thenReturn(null);
        when(reviews.GetRecensioni(7)).thenReturn(null);
        HttpSession session = session(null);
        HttpServletRequest request = filmRequest(session, "7");
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/WEB-INF/jsp/film.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, mock(HttpServletResponse.class));

        verify(session).setAttribute(eq("Generi"), eq(Collections.emptyList()));
        verify(session).setAttribute(eq("recensioni"), eq(Collections.emptyList()));
        verify(session).removeAttribute("users");
        verify(session).setAttribute("watched", false);
        verify(session).setAttribute("inwatchlist", false);
    }

    @Test
    void visualizzaFilmLoadsReviewUsersAndUserState() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        RecensioniService reviews = mock(RecensioniService.class);
        ProfileService profile = mock(ProfileService.class);
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        inject(servlet, "catalogoService", catalog);
        inject(servlet, "recensioniService", reviews);
        inject(servlet, "profileService", profile);
        FilmBean current = new FilmBean();
        current.setIdFilm(7);
        FilmBean other = new FilmBean();
        other.setIdFilm(99);
        RecensioneBean review = new RecensioneBean();
        List<RecensioneBean> reviewList = Collections.singletonList(review);
        when(catalog.getFilm(7)).thenReturn(current);
        when(catalog.getGeneri(7)).thenReturn(Collections.emptyList());
        when(reviews.GetRecensioni(7)).thenReturn(reviewList);
        when(profile.getUsers(reviewList)).thenReturn(new HashMap<>());
        when(profile.retrieveWatchedFilms("tester")).thenReturn(List.of(other, current));
        when(profile.retrieveWatchlist("tester")).thenReturn(List.of(other, current));
        HashMap<String, model.Entity.ValutazioneBean> ratings = new HashMap<>();
        when(reviews.GetValutazioni(7, "user@example.com")).thenReturn(ratings);
        HttpSession session = session(user("UTENTE"));
        HttpServletRequest request = filmRequest(session, "7");
        when(request.getRequestDispatcher("/WEB-INF/jsp/film.jsp")).thenReturn(mock(RequestDispatcher.class));

        servlet.doGet(request, mock(HttpServletResponse.class));

        verify(session).setAttribute("watched", true);
        verify(session).setAttribute("inwatchlist", true);
        verify(session).setAttribute("valutazioni", ratings);
        verify(session).setAttribute(eq("users"), any(HashMap.class));
    }

    @Test
    void visualizzaFilmIsolatesRelatedAndUserContextFailures() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        RecensioniService reviews = mock(RecensioniService.class);
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        inject(servlet, "catalogoService", catalog);
        inject(servlet, "recensioniService", reviews);
        when(catalog.getFilm(7)).thenReturn(new FilmBean());
        when(catalog.getGeneri(7)).thenThrow(new IllegalStateException("genres"));
        when(reviews.GetValutazioni(anyInt(), anyString())).thenThrow(new IllegalStateException("ratings"));
        HttpSession session = session(user("UTENTE"));
        HttpServletRequest request = filmRequest(session, "7");
        when(request.getRequestDispatcher("/WEB-INF/jsp/film.jsp")).thenReturn(mock(RequestDispatcher.class));

        assertDoesNotThrow(() -> servlet.doGet(request, mock(HttpServletResponse.class)));
        verify(session).setAttribute(eq("recensioni"), eq(Collections.emptyList()));
        verify(session).setAttribute("watched", false);
    }

    @Test
    void visualizzaFilmHandlesForwardGlobalAndResponseFailures() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        inject(servlet, "catalogoService", catalog);
        when(catalog.getFilm(7)).thenReturn(new FilmBean());
        HttpSession session = session(null);
        HttpServletRequest request = filmRequest(session, "7");
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/WEB-INF/jsp/film.jsp")).thenReturn(dispatcher);
        doThrow(new ServletException("forward")).when(dispatcher).forward(any(), any());
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doGet(request, response);
        verify(response).sendError(500, "Errore nel caricamento della vista film.");

        HttpServletRequest globalFailure = mock(HttpServletRequest.class);
        when(globalFailure.getSession(true)).thenThrow(new IllegalStateException("session"));
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(globalFailure, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doGet(globalFailure, broken));
    }

    @Test
    void visualizzaFilmHandlesRedirectIoAndPostDelegationFailure() throws Exception {
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendRedirect("catalogo.jsp");
        assertDoesNotThrow(() -> servlet.doGet(filmRequest(mock(HttpSession.class), null), broken));

        VisualizzaFilmServlet spy = spy(new VisualizzaFilmServlet());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IOException("delegation")).when(spy).doGet(request, response);
        spy.doPost(request, response);
        verify(response).sendError(500, "Errore interno durante l'elaborazione della richiesta.");
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
    void visualizzaFilmSkipsRedirectAfterResponseCommit() throws Exception {
        VisualizzaFilmServlet servlet = new VisualizzaFilmServlet();
        java.lang.reflect.Method redirect = VisualizzaFilmServlet.class
                .getDeclaredMethod("redirectSafe", HttpServletResponse.class, String.class);
        redirect.setAccessible(true);
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);

        redirect.invoke(servlet, committed, "/catalogo");

        verify(committed, never()).sendRedirect(anyString());
    }
}
