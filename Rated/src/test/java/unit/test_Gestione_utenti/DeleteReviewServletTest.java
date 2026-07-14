package unit.test_Gestione_utenti;

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

class DeleteReviewServletTest {

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
    void deleteReviewCoversGetAnonymousAndValidationBranches() throws Exception {
        DeleteReviewServlet servlet = new DeleteReviewServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(null);
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(getResponse).sendRedirect("/profile");
        assertDoesNotThrow(() -> servlet.doGet(request, getResponse));

        HttpServletResponse anonymous = mock(HttpServletResponse.class);
        when(request.getSession(true)).thenReturn(null);
        servlet.doPost(request, anonymous);
        verify(anonymous).sendRedirect("/login.jsp");

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        when(request.getSession(true)).thenReturn(session);
        HttpServletResponse missingId = mock(HttpServletResponse.class);
        servlet.doPost(request, missingId);
        verify(missingId).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        when(request.getParameter("DeleteFilmID")).thenReturn("");
        HttpServletResponse emptyId = mock(HttpServletResponse.class);
        servlet.doPost(request, emptyId);
        verify(emptyId).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void deleteReviewSuccessAndFailureBranchesAreCovered() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        DeleteReviewServlet servlet = new DeleteReviewServlet();
        inject(servlet, "RecensioniService", reviews);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("DeleteFilmID")).thenReturn("8");
        when(request.getContextPath()).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doPost(request, response);
        verify(reviews).deleteRecensione("user@example.com", 8);
        verify(response).sendRedirect("/profile?visitedUser=test-user");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendRedirect(anyString());

        doThrow(new IllegalStateException("failure")).when(reviews).deleteRecensione(anyString(), anyInt());
        HttpServletResponse error = mock(HttpServletResponse.class);
        servlet.doPost(request, error);
        verify(error).sendError(500, "Si è verificato un errore critico imprevisto.");

        HttpServletResponse sendErrorFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(sendErrorFailure)
                .sendError(500, "Si è verificato un errore critico imprevisto.");
        assertDoesNotThrow(() -> servlet.doPost(request, sendErrorFailure));
    }

    @Test
    void deleteReviewCoversNonNullRedirectContexts() throws Exception {
        DeleteReviewServlet servlet = new DeleteReviewServlet();
        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getContextPath()).thenReturn("/Rated");
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        servlet.doGet(get, getResponse);
        verify(getResponse).sendRedirect("/Rated/profile");

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        when(anonymous.getSession(true)).thenReturn(null);
        when(anonymous.getContextPath()).thenReturn("/Rated");
        HttpServletResponse anonymousResponse = mock(HttpServletResponse.class);
        servlet.doPost(anonymous, anonymousResponse);
        verify(anonymousResponse).sendRedirect("/Rated/login.jsp");

        HttpServletResponse brokenLogin = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(brokenLogin).sendRedirect("/Rated/login.jsp");
        assertDoesNotThrow(() -> servlet.doPost(anonymous, brokenLogin));

        RecensioniService reviews = mock(RecensioniService.class);
        inject(servlet, "RecensioniService", reviews);
        doThrow(new IllegalStateException("service")).when(reviews).deleteRecensione(anyString(), anyInt());
        HttpSession authenticated = mock(HttpSession.class);
        when(authenticated.getAttribute("user")).thenReturn(user());
        HttpServletRequest failed = mock(HttpServletRequest.class);
        when(failed.getSession(true)).thenReturn(authenticated);
        when(failed.getParameter("DeleteFilmID")).thenReturn("7");
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(failed, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }
}
