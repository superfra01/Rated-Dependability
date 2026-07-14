package unit.test_Gestione_recensioni;

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

class RimuoviReviewAndWarnServletTest {

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
    void removeAndWarnCoversAuthorizationValidationAndSuccess() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        ModerationService moderation = mock(ModerationService.class);
        RimuoviReviewAndWarnServlet servlet = new RimuoviReviewAndWarnServlet();
        inject(servlet, "RecensioniService", reviews);
        inject(servlet, "ModerationService", moderation);

        HttpServletResponse getFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(getFailure).sendRedirect("/moderator");
        servlet.doGet(mock(HttpServletRequest.class), getFailure);

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession(true)).thenReturn(anonymousSession);
        HttpServletResponse writerFailure = mock(HttpServletResponse.class);
        when(writerFailure.getWriter()).thenThrow(new IOException("closed"));
        servlet.doPost(anonymous, writerFailure);

        HttpServletRequest invalid = moderatorRequest();
        when(invalid.getParameter("idFilm")).thenReturn("");
        HttpServletResponse invalidResponse = mock(HttpServletResponse.class);
        servlet.doPost(invalid, invalidResponse);
        verify(invalidResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest request = moderatorRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doPost(request, response);
        verify(reviews).deleteRecensione("author@example.com", 7);
        verify(moderation).warn("author@example.com");
        verify(response).sendRedirect("/Rated/moderator");
    }

    @Test
    void removeAndWarnCoversRedirectAndCriticalFailureBranches() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        RimuoviReviewAndWarnServlet servlet = new RimuoviReviewAndWarnServlet();
        inject(servlet, "RecensioniService", reviews);
        HttpServletRequest request = moderatorRequest();
        when(request.getContextPath()).thenReturn("/rated");
        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/rated/moderator");
        servlet.doPost(request, redirectFailure);

        doThrow(new IllegalStateException("service")).when(reviews).deleteRecensione(anyString(), anyInt());
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doPost(request, broken);

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }

    @Test
    void removeAndWarnCoversRemainingShortCircuitAndCommittedBranches() throws Exception {
        RimuoviReviewAndWarnServlet servlet = new RimuoviReviewAndWarnServlet();

        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getContextPath()).thenReturn("/Rated");
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        servlet.doGet(get, getResponse);
        verify(getResponse).sendRedirect("/Rated/moderator");

        HttpServletRequest noSession = mock(HttpServletRequest.class);
        when(noSession.getSession(true)).thenReturn(null);
        servlet.doPost(noSession, writableResponse());

        HttpServletRequest missingEmail = moderatorRequest();
        when(missingEmail.getParameter("ReviewUserEmail")).thenReturn(null);
        servlet.doPost(missingEmail, mock(HttpServletResponse.class));

        HttpServletRequest missingId = moderatorRequest();
        when(missingId.getParameter("idFilm")).thenReturn(null);
        servlet.doPost(missingId, mock(HttpServletResponse.class));

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(moderatorRequest(), committed);
        verify(committed, never()).sendRedirect(anyString());
    }
}
