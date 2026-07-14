package unit.test_Gestione_recensioni;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import integration.DatabaseSetupForTest;
import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.RicercaCatalogoServlet;
import sottosistemi.Gestione_Catalogo.view.ValutaFilmServlet;
import sottosistemi.Gestione_Catalogo.view.VisualizzaCatalogoServlet;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Recensioni.view.ReportReviewServlet;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.DeleteReviewServlet;

class ReportReviewServletTest {

    private static final String ERROR_CONTENT_TYPE = "text/plain;charset=UTF-8";

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static UtenteBean user() {
        UtenteBean user = new UtenteBean();
        user.setEmail("user@example.com");
        user.setUsername("test-user");
        return user;
    }

    @Test
    void reportReviewGetUsesEmptyContextAndHandlesRedirectFailure() throws Exception {
        ReportReviewServlet servlet = new ReportReviewServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn(null);
        doThrow(new IOException("closed")).when(response).sendRedirect("/catalogo");

        assertDoesNotThrow(() -> servlet.doGet(request, response));
        verify(response).sendRedirect("/catalogo");
    }

    @Test
    void reportReviewRejectsAnonymousUserWithAndWithoutCommittedResponse() throws Exception {
        ReportReviewServlet servlet = new ReportReviewServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(true)).thenReturn(null);
        when(request.getContextPath()).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("/login.jsp");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendRedirect(anyString());
    }

    @Test
    void reportReviewHandlesLoginRedirectIOException() throws Exception {
        ReportReviewServlet servlet = new ReportReviewServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(true)).thenReturn(null);
        doThrow(new IOException("closed")).when(response).sendRedirect("/login.jsp");

        assertDoesNotThrow(() -> servlet.doPost(request, response));
    }

    @Test
    void reportReviewValidatesEachParameterBranch() throws Exception {
        ReportReviewServlet servlet = new ReportReviewServlet();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());

        HttpServletRequest missingReviewer = mock(HttpServletRequest.class);
        HttpServletResponse response1 = mock(HttpServletResponse.class);
        when(missingReviewer.getSession(true)).thenReturn(session);
        servlet.doPost(missingReviewer, response1);
        verify(response1).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest missingId = mock(HttpServletRequest.class);
        HttpServletResponse response2 = mock(HttpServletResponse.class);
        when(missingId.getSession(true)).thenReturn(session);
        when(missingId.getParameter("reviewerEmail")).thenReturn("author@example.com");
        servlet.doPost(missingId, response2);
        verify(response2).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest emptyId = mock(HttpServletRequest.class);
        HttpServletResponse response3 = mock(HttpServletResponse.class);
        when(emptyId.getSession(true)).thenReturn(session);
        when(emptyId.getParameter("reviewerEmail")).thenReturn("author@example.com");
        when(emptyId.getParameter("idFilm")).thenReturn("");
        servlet.doPost(emptyId, response3);
        verify(response3).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest malformedId = mock(HttpServletRequest.class);
        HttpServletResponse response4 = mock(HttpServletResponse.class);
        when(malformedId.getSession(true)).thenReturn(session);
        when(malformedId.getParameter("reviewerEmail")).thenReturn("author@example.com");
        when(malformedId.getParameter("idFilm")).thenReturn("not-a-number");
        servlet.doPost(malformedId, response4);
        verify(response4).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void reportReviewSuccessCoversRedirectBranches() throws Exception {
        RecensioniService service = mock(RecensioniService.class);
        ReportReviewServlet servlet = new ReportReviewServlet();
        inject(servlet, "RecensioniService", service);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("reviewerEmail")).thenReturn("author@example.com");
        when(request.getParameter("idFilm")).thenReturn("42");
        when(request.getContextPath()).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.doPost(request, response);
        verify(service).report("user@example.com", "author@example.com", 42);
        verify(response).sendRedirect("/film?idFilm=42");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendRedirect(anyString());
    }

    @Test
    void reportReviewHandlesFinalRedirectAndCriticalErrorFailures() throws Exception {
        RecensioniService service = mock(RecensioniService.class);
        ReportReviewServlet servlet = new ReportReviewServlet();
        inject(servlet, "RecensioniService", service);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("reviewerEmail")).thenReturn("author@example.com");
        when(request.getParameter("idFilm")).thenReturn("42");

        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/film?idFilm=42");
        assertDoesNotThrow(() -> servlet.doPost(request, redirectFailure));

        doThrow(new IllegalStateException("service failure"))
                .when(service).report(anyString(), anyString(), anyInt());
        HttpServletResponse errorResponse = mock(HttpServletResponse.class);
        servlet.doPost(request, errorResponse);
        verify(errorResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore imprevisto nel sistema.");

        HttpServletResponse sendErrorFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(sendErrorFailure)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore imprevisto nel sistema.");
        assertDoesNotThrow(() -> servlet.doPost(request, sendErrorFailure));
    }

    @Test
    void reportReviewCoversNonNullContextsAndCommittedCriticalError() throws Exception {
        ReportReviewServlet servlet = new ReportReviewServlet();
        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getContextPath()).thenReturn("/Rated");
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        servlet.doGet(get, getResponse);
        verify(getResponse).sendRedirect("/Rated/catalogo");

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        when(anonymous.getSession(true)).thenReturn(null);
        when(anonymous.getContextPath()).thenReturn("/Rated");
        HttpServletResponse anonymousResponse = mock(HttpServletResponse.class);
        servlet.doPost(anonymous, anonymousResponse);
        verify(anonymousResponse).sendRedirect("/Rated/login.jsp");

        RecensioniService failedService = mock(RecensioniService.class);
        doThrow(new IllegalStateException("service")).when(failedService).report(anyString(), anyString(), anyInt());
        inject(servlet, "RecensioniService", failedService);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("reviewerEmail")).thenReturn("author@example.com");
        when(request.getParameter("idFilm")).thenReturn("7");
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }
}
