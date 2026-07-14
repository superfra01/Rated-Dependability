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

class AggiungiWatchlistServletTest {

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
    void watchlistCoversWriterFailuresBothActionsAndGetFailure() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        AggiungiWatchlistServlet servlet = new AggiungiWatchlistServlet();
        inject(servlet, "profileService", profile);

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession()).thenReturn(anonymousSession);
        HttpServletResponse brokenWriter = mock(HttpServletResponse.class);
        when(brokenWriter.getWriter()).thenThrow(new IOException("closed"));
        servlet.doPost(anonymous, brokenWriter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession authenticated = session(user("UTENTE"));
        when(request.getSession()).thenReturn(authenticated);
        when(request.getParameter("filmId")).thenReturn("bad");
        servlet.doPost(request, brokenWriter);

        when(request.getParameter("filmId")).thenReturn("7");
        when(profile.isFilmInWatchlist("user@example.com", 7)).thenReturn(false, true);
        servlet.doPost(request, brokenWriter);
        servlet.doPost(request, writableResponse());
        verify(profile).aggiungiAllaWatchlist("user@example.com", 7);
        verify(profile).rimuoviDallaWatchlist("user@example.com", 7);

        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/catalogo");
        servlet.doGet(mock(HttpServletRequest.class), redirectFailure);
    }

    @Test
    void watchlistCoversServiceCommittedAndSendErrorIo() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        AggiungiWatchlistServlet servlet = new AggiungiWatchlistServlet();
        inject(servlet, "profileService", profile);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession authenticated = session(user("UTENTE"));
        when(request.getSession()).thenReturn(authenticated);
        when(request.getParameter("filmId")).thenReturn("7");
        when(profile.isFilmInWatchlist(anyString(), anyInt())).thenThrow(new IllegalStateException("service"));
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doPost(request, broken);

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }
    @Test
    void watchlistRejectsBothNullAndEmptyFilmIds() throws Exception {
        AggiungiWatchlistServlet servlet = new AggiungiWatchlistServlet();
        HttpSession authenticated = session(user("UTENTE"));

        for (String filmId : new String[] {null, ""}) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getSession()).thenReturn(authenticated);
            when(request.getParameter("filmId")).thenReturn(filmId);
            HttpServletResponse response = writableResponse();
            servlet.doPost(request, response);
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            verify(response).setCharacterEncoding("UTF-8");
        }
    }

    @Test
    void watchlistGetPerformsTheNormalRedirect() throws Exception {
        AggiungiWatchlistServlet servlet = new AggiungiWatchlistServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/Rated");
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/Rated/catalogo");
    }
}
