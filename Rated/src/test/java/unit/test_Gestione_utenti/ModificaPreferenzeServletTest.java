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

class ModificaPreferenzeServletTest {

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
    void modifyPreferencesCoversAnonymousForbiddenAndSuccessRedirects() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ModificaPreferenzeServlet servlet = new ModificaPreferenzeServlet();
        inject(servlet, "profileService", profile);

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession()).thenReturn(anonymousSession);
        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("login.jsp");
        servlet.doPost(anonymous, redirectFailure);

        HttpServletRequest forbidden = mock(HttpServletRequest.class);
        HttpSession authenticated = session(user("UTENTE"));
        when(forbidden.getSession()).thenReturn(authenticated);
        when(forbidden.getParameter("email")).thenReturn("other@example.com");
        HttpServletResponse forbiddenFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(forbiddenFailure).sendError(eq(403), anyString());
        servlet.doPost(forbidden, forbiddenFailure);

        HttpServletRequest valid = mock(HttpServletRequest.class);
        when(valid.getSession()).thenReturn(authenticated);
        when(valid.getParameterValues("selectedGenres")).thenReturn(new String[] {"Drama"});
        HttpServletResponse profileRedirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(profileRedirectFailure).sendRedirect("profile?visitedUser=tester");
        servlet.doPost(valid, profileRedirectFailure);
        verify(profile).aggiornaPreferenzeUtente(eq("user@example.com"), any(String[].class));
    }

    @Test
    void modifyPreferencesCoversServiceCommittedAndGetErrorBranches() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ModificaPreferenzeServlet servlet = new ModificaPreferenzeServlet();
        inject(servlet, "profileService", profile);
        doThrow(new IllegalStateException("service")).when(profile).aggiornaPreferenzeUtente(anyString(), any());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession authenticated = session(user("UTENTE"));
        when(request.getSession()).thenReturn(authenticated);
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doPost(request, broken);

        HttpServletResponse getBroken = mock(HttpServletResponse.class);
        doThrow(new IOException("redirect")).when(getBroken).sendRedirect("profile.jsp");
        doThrow(new IOException("error")).when(getBroken).sendError(anyInt(), anyString());
        servlet.doGet(request, getBroken);

        HttpServletResponse getError = mock(HttpServletResponse.class);
        doThrow(new IOException("redirect")).when(getError).sendRedirect("profile.jsp");
        servlet.doGet(request, getError);
        verify(getError).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Errore durante il reindirizzamento.");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        doThrow(new IOException("redirect")).when(committed).sendRedirect("profile.jsp");
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);

        HttpServletResponse committedPost = mock(HttpServletResponse.class);
        when(committedPost.isCommitted()).thenReturn(true);
        servlet.doPost(request, committedPost);
        verify(committedPost, never()).sendError(anyInt(), anyString());
    }

}
