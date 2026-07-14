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

class VoteReviewServletTest {

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
    void voteReviewCoversGetAnonymousValidationAndSuccess() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        VoteReviewServlet servlet = new VoteReviewServlet();
        inject(servlet, "RecensioniService", reviews);
        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/catalogo");
        servlet.doGet(mock(HttpServletRequest.class), redirectFailure);

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession(true)).thenReturn(anonymousSession);
        HttpServletResponse brokenWriter = mock(HttpServletResponse.class);
        when(brokenWriter.getWriter()).thenThrow(new IOException("closed"));
        servlet.doPost(anonymous, brokenWriter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession authenticated = session(user("UTENTE"));
        when(request.getSession(true)).thenReturn(authenticated);
        servlet.doPost(request, mock(HttpServletResponse.class));
        when(request.getParameter("idFilm")).thenReturn("7");
        when(request.getParameter("emailRecensore")).thenReturn("author@example.com");
        when(request.getParameter("valutazione")).thenReturn("true");
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doPost(request, response);
        verify(reviews).addValutazione("user@example.com", 7, "author@example.com", true);
        verify(response).setStatus(200);
    }

    @Test
    void voteReviewCoversMalformedServiceCommittedAndErrorIo() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        VoteReviewServlet servlet = new VoteReviewServlet();
        inject(servlet, "RecensioniService", reviews);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession authenticated = session(user("UTENTE"));
        when(request.getSession(true)).thenReturn(authenticated);
        when(request.getParameter("idFilm")).thenReturn("bad");
        when(request.getParameter("emailRecensore")).thenReturn("author@example.com");
        when(request.getParameter("valutazione")).thenReturn("false");
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        when(request.getParameter("idFilm")).thenReturn("7");
        doThrow(new IllegalStateException("service")).when(reviews)
                .addValutazione(anyString(), anyInt(), anyString(), eq(false));
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doPost(request, broken);
    }

    @Test
    void voteReviewCoversRemainingSessionContextAndParameterBranches() throws Exception {
        VoteReviewServlet servlet = new VoteReviewServlet();

        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getContextPath()).thenReturn("/Rated");
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        servlet.doGet(get, getResponse);
        verify(getResponse).sendRedirect("/Rated/catalogo");

        HttpServletRequest noSession = mock(HttpServletRequest.class);
        when(noSession.getSession(true)).thenReturn(null);
        servlet.doPost(noSession, writableResponse());

        HttpSession authenticated = session(user("UTENTE"));
        HttpServletRequest missingReviewer = mock(HttpServletRequest.class);
        when(missingReviewer.getSession(true)).thenReturn(authenticated);
        when(missingReviewer.getParameter("idFilm")).thenReturn("7");
        servlet.doPost(missingReviewer, mock(HttpServletResponse.class));

        HttpServletRequest missingVote = mock(HttpServletRequest.class);
        when(missingVote.getSession(true)).thenReturn(authenticated);
        when(missingVote.getParameter("idFilm")).thenReturn("7");
        when(missingVote.getParameter("emailRecensore")).thenReturn("author@example.com");
        servlet.doPost(missingVote, mock(HttpServletResponse.class));
    }

    @Test
    void voteReviewVerifiesEveryHttpErrorResponse() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        VoteReviewServlet servlet = new VoteReviewServlet();
        inject(servlet, "RecensioniService", reviews);

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession(true)).thenReturn(anonymousSession);
        HttpServletResponse unauthorized = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(unauthorized.getWriter()).thenReturn(writer);
        servlet.doPost(anonymous, unauthorized);
        verify(unauthorized).setContentType("text/plain");
        verify(unauthorized).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Devi essere autenticato per votare una recensione.");

        HttpServletRequest missingParameters = mock(HttpServletRequest.class);
        HttpSession authenticatedSession = session(user("UTENTE"));
        when(missingParameters.getSession(true)).thenReturn(authenticatedSession);
        HttpServletResponse badRequest = mock(HttpServletResponse.class);
        servlet.doPost(missingParameters, badRequest);
        verify(badRequest).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest serviceFailure = mock(HttpServletRequest.class);
        HttpSession serviceFailureSession = session(user("UTENTE"));
        when(serviceFailure.getSession(true)).thenReturn(serviceFailureSession);
        when(serviceFailure.getParameter("idFilm")).thenReturn("7");
        when(serviceFailure.getParameter("emailRecensore")).thenReturn("author@example.com");
        when(serviceFailure.getParameter("valutazione")).thenReturn("true");
        doThrow(new IllegalStateException("service")).when(reviews)
                .addValutazione("user@example.com", 7, "author@example.com", true);
        HttpServletResponse critical = mock(HttpServletResponse.class);
        servlet.doPost(serviceFailure, critical);
        verify(critical).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Si è verificato un errore critico imprevisto.");
    }
}
