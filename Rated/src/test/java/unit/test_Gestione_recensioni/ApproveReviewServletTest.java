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

class ApproveReviewServletTest {

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
    void approveReviewCoversGetAuthorizationAndValidation() throws Exception {
        ApproveReviewServlet servlet = new ApproveReviewServlet();
        HttpServletRequest get = mock(HttpServletRequest.class);
        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/moderator");
        servlet.doGet(get, redirectFailure);

        for (UtenteBean user : new UtenteBean[] {null, user("UTENTE")}) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpSession currentSession = session(user);
            when(request.getSession(true)).thenReturn(currentSession);
            HttpServletResponse response = writableResponse();
            servlet.doPost(request, response);
            verify(response).setStatus(401);
        }

        HttpServletRequest brokenWriterRequest = mock(HttpServletRequest.class);
        HttpSession anonymous = session(null);
        when(brokenWriterRequest.getSession(true)).thenReturn(anonymous);
        HttpServletResponse brokenWriter = mock(HttpServletResponse.class);
        when(brokenWriter.getWriter()).thenThrow(new IOException("closed"));
        servlet.doPost(brokenWriterRequest, brokenWriter);

        HttpServletRequest invalid = moderatorRequest();
        when(invalid.getParameter("ReviewUserEmail")).thenReturn(null);
        HttpServletResponse missingEmail = mock(HttpServletResponse.class);
        servlet.doPost(invalid, missingEmail);
        verify(missingEmail).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        when(invalid.getParameter("ReviewUserEmail")).thenReturn("author@example.com");
        when(invalid.getParameter("idFilm")).thenReturn("bad");
        HttpServletResponse malformedId = mock(HttpServletResponse.class);
        servlet.doPost(invalid, malformedId);
        verify(malformedId).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void approveReviewCoversSuccessRedirectAndCriticalFailures() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        ApproveReviewServlet servlet = new ApproveReviewServlet();
        inject(servlet, "RecensioniService", reviews);
        HttpServletRequest request = moderatorRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doPost(request, response);
        verify(reviews).deleteReports("author@example.com", 7);
        verify(response).sendRedirect("/moderator");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendRedirect(anyString());

        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/moderator");
        servlet.doPost(request, redirectFailure);

        doThrow(new IllegalStateException("service")).when(reviews).deleteReports(anyString(), anyInt());
        HttpServletResponse errorFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(errorFailure).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(request, errorFailure));
    }

    @Test
    void approveReviewCoversRemainingShortCircuitAndResponseBranches() throws Exception {
        ApproveReviewServlet servlet = new ApproveReviewServlet();

        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getContextPath()).thenReturn("/Rated");
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        servlet.doGet(get, getResponse);
        verify(getResponse).sendRedirect("/Rated/moderator");

        HttpServletRequest noSession = mock(HttpServletRequest.class);
        when(noSession.getSession(true)).thenReturn(null);
        servlet.doPost(noSession, writableResponse());

        HttpServletRequest missingId = moderatorRequest();
        when(missingId.getParameter("idFilm")).thenReturn(null);
        servlet.doPost(missingId, mock(HttpServletResponse.class));

        HttpServletRequest emptyId = moderatorRequest();
        when(emptyId.getParameter("idFilm")).thenReturn("");
        servlet.doPost(emptyId, mock(HttpServletResponse.class));

        RecensioniService failedService = mock(RecensioniService.class);
        doThrow(new IllegalStateException("service")).when(failedService).deleteReports(anyString(), anyInt());
        ApproveReviewServlet failed = new ApproveReviewServlet();
        inject(failed, "RecensioniService", failedService);
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        failed.doPost(moderatorRequest(), committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }
}
