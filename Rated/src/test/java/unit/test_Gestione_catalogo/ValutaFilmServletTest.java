package unit.test_Gestione_catalogo;

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

class ValutaFilmServletTest {

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
    void valutaFilmGetAndAnonymousBranchesAreCovered() throws Exception {
        ValutaFilmServlet servlet = new ValutaFilmServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn(null);
        doThrow(new IOException("closed")).when(response).sendRedirect("/catalogo");
        assertDoesNotThrow(() -> servlet.doGet(request, response));

        HttpServletResponse loginResponse = mock(HttpServletResponse.class);
        when(request.getSession(true)).thenReturn(null);
        servlet.doPost(request, loginResponse);
        verify(loginResponse).sendRedirect("/login.jsp");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendRedirect(anyString());
    }

    @Test
    void valutaFilmValidatesMissingAndMalformedParameters() throws Exception {
        ValutaFilmServlet servlet = new ValutaFilmServlet();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());

        HttpServletRequest missingId = mock(HttpServletRequest.class);
        HttpServletResponse response1 = mock(HttpServletResponse.class);
        when(missingId.getSession(true)).thenReturn(session);
        servlet.doPost(missingId, response1);
        verify(response1).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest missingRating = mock(HttpServletRequest.class);
        HttpServletResponse response2 = mock(HttpServletResponse.class);
        when(missingRating.getSession(true)).thenReturn(session);
        when(missingRating.getParameter("idFilm")).thenReturn("7");
        servlet.doPost(missingRating, response2);
        verify(response2).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        HttpServletRequest malformed = mock(HttpServletRequest.class);
        HttpServletResponse response3 = mock(HttpServletResponse.class);
        when(malformed.getSession(true)).thenReturn(session);
        when(malformed.getParameter("idFilm")).thenReturn("bad");
        when(malformed.getParameter("valutazione")).thenReturn("5");
        servlet.doPost(malformed, response3);
        verify(response3).sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Si è verificato un errore critico imprevisto.");
    }

    @Test
    void valutaFilmSuccessCoversWatchedAndRedirectBranches() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        ProfileService profile = mock(ProfileService.class);
        ValutaFilmServlet servlet = new ValutaFilmServlet();
        inject(servlet, "recensioniService", reviews);
        inject(servlet, "profileService", profile);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("idFilm")).thenReturn("7");
        when(request.getParameter("valutazione")).thenReturn("5");
        when(request.getParameter("titolo")).thenReturn("Titolo");
        when(request.getParameter("recensione")).thenReturn("Testo");
        when(request.getContextPath()).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(profile.isFilmVisto("user@example.com", 7)).thenReturn(false);
        servlet.doPost(request, response);
        verify(reviews).addRecensione("user@example.com", 7, "Testo", "Titolo", 5);
        verify(profile).aggiungiFilmVisto("user@example.com", 7);
        verify(response).sendRedirect("/film?idFilm=7");

        when(profile.isFilmVisto("user@example.com", 7)).thenReturn(true);
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(committed, never()).sendRedirect(anyString());
    }

    @Test
    void valutaFilmHandlesRedirectAndSendErrorIOException() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        ProfileService profile = mock(ProfileService.class);
        ValutaFilmServlet servlet = new ValutaFilmServlet();
        inject(servlet, "recensioniService", reviews);
        inject(servlet, "profileService", profile);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("idFilm")).thenReturn("7");
        when(request.getParameter("valutazione")).thenReturn("5");

        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/film?idFilm=7");
        assertDoesNotThrow(() -> servlet.doPost(request, redirectFailure));

        doThrow(new IllegalStateException("failure")).when(reviews)
                .addRecensione(anyString(), anyInt(), eq(null), eq(null), anyInt());
        HttpServletResponse sendErrorFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(sendErrorFailure)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Si è verificato un errore critico imprevisto.");
        assertDoesNotThrow(() -> servlet.doPost(request, sendErrorFailure));
    }

    @Test
    void valutaFilmCoversNonNullContextsAndCommittedCriticalError() throws Exception {
        ValutaFilmServlet servlet = new ValutaFilmServlet();

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

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user());
        HttpServletRequest malformed = mock(HttpServletRequest.class);
        when(malformed.getSession(true)).thenReturn(session);
        when(malformed.getParameter("idFilm")).thenReturn("bad");
        when(malformed.getParameter("valutazione")).thenReturn("5");
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(malformed, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }
}
