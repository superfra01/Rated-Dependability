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

class AggiungiFilmServletTest {

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

    @Test
    void addFilmCoversGetAuthorizationAndWriterFailure() throws Exception {
        AggiungiFilmServlet servlet = new AggiungiFilmServlet();
        servlet.doGet(mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        for (UtenteBean user : new UtenteBean[] {null, user("UTENTE")}) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpSession userSession = session(user);
            when(request.getSession(true)).thenReturn(userSession);
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(response.getWriter()).thenReturn(mock(PrintWriter.class));
            servlet.doPost(request, response);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(request.getSession(true)).thenReturn(anonymousSession);
        HttpServletResponse broken = mock(HttpServletResponse.class);
        when(broken.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(request, broken));
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
    void addFilmSuccessSupportsPresentAndMissingPoster() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        AggiungiFilmServlet servlet = new AggiungiFilmServlet();
        inject(servlet, "catalogoService", catalog);
        HttpServletRequest noPoster = addFilmRequest();
        servlet.doPost(noPoster, mock(HttpServletResponse.class));
        verify(catalog).addFilm(eq(2024), eq(null), eq(120), eq(null), eq(null), eq("Film"), eq(null), eq(null));

        HttpServletRequest withPoster = addFilmRequest();
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(2L);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {4, 2}));
        when(withPoster.getPart("locandinaFilm")).thenReturn(part);
        servlet.doPost(withPoster, mock(HttpServletResponse.class));
        verify(catalog).addFilm(eq(2024), eq(null), eq(120), eq(null), any(byte[].class), eq("Film"), eq(null), eq(null));
    }

    @Test
    void addFilmHandlesPosterNumberAndServiceFailures() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        AggiungiFilmServlet servlet = new AggiungiFilmServlet();
        inject(servlet, "catalogoService", catalog);
        HttpServletRequest posterFailure = addFilmRequest();
        when(posterFailure.getPart("locandinaFilm")).thenThrow(new ServletException("part"));
        HttpServletResponse posterResponse = mock(HttpServletResponse.class);
        servlet.doPost(posterFailure, posterResponse);
        verify(posterResponse).sendError(500);

        HttpServletRequest invalidNumber = addFilmRequest();
        when(invalidNumber.getParameter("annoFilm")).thenReturn("bad");
        HttpServletResponse writerFailure = mock(HttpServletResponse.class);
        when(writerFailure.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(invalidNumber, writerFailure));

        HttpServletRequest serviceFailure = addFilmRequest();
        doThrow(new IllegalStateException("service")).when(catalog)
                .addFilm(anyInt(), any(), anyInt(), any(), any(), any(), any(), any());
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(serviceFailure, broken));
    }
    @Test
    void addFilmCoversEmptyPosterAndCommittedFailureResponses() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        AggiungiFilmServlet servlet = new AggiungiFilmServlet();
        inject(servlet, "catalogoService", catalog);

        HttpServletRequest emptyPosterRequest = addFilmRequest();
        Part emptyPoster = mock(Part.class);
        when(emptyPosterRequest.getPart("locandinaFilm")).thenReturn(emptyPoster);
        servlet.doPost(emptyPosterRequest, mock(HttpServletResponse.class));

        HttpServletRequest posterFailure = addFilmRequest();
        when(posterFailure.getPart("locandinaFilm")).thenThrow(new ServletException("part"));
        HttpServletResponse committedPoster = mock(HttpServletResponse.class);
        when(committedPoster.isCommitted()).thenReturn(true);
        servlet.doPost(posterFailure, committedPoster);
        verify(committedPoster, never()).sendError(anyInt());

        HttpServletRequest invalidNumber = addFilmRequest();
        when(invalidNumber.getParameter("annoFilm")).thenReturn("bad");
        HttpServletResponse committedNumber = mock(HttpServletResponse.class);
        when(committedNumber.isCommitted()).thenReturn(true);
        servlet.doPost(invalidNumber, committedNumber);
        verify(committedNumber, never()).setStatus(anyInt());

        doThrow(new IllegalStateException("service")).when(catalog)
                .addFilm(anyInt(), any(), anyInt(), any(), any(), any(), any(), any());
        HttpServletResponse committedService = mock(HttpServletResponse.class);
        when(committedService.isCommitted()).thenReturn(true);
        servlet.doPost(addFilmRequest(), committedService);
        verify(committedService, never()).sendError(anyInt(), anyString());
    }
    @Test
    void addFilmHandlesFailureWhileSendingPosterError() throws Exception {
        AggiungiFilmServlet servlet = new AggiungiFilmServlet();
        HttpServletRequest request = addFilmRequest();
        when(request.getPart("locandinaFilm")).thenThrow(new ServletException("part"));
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        assertDoesNotThrow(() -> servlet.doPost(request, response));
    }

    @Test
    void addFilmVerifiesEmptyPosterAndEveryNormalErrorResponse() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        AggiungiFilmServlet servlet = new AggiungiFilmServlet();
        inject(servlet, "catalogoService", catalog);

        HttpServletRequest emptyPosterRequest = addFilmRequest();
        Part emptyPoster = mock(Part.class);
        when(emptyPosterRequest.getPart("locandinaFilm")).thenReturn(emptyPoster);
        servlet.doPost(emptyPosterRequest, mock(HttpServletResponse.class));
        verify(catalog).addFilm(eq(2024), eq(null), eq(120), eq(null), eq(null),
                eq("Film"), eq(null), eq(null));

        HttpServletRequest invalidNumber = addFilmRequest();
        when(invalidNumber.getParameter("annoFilm")).thenReturn("invalid");
        HttpServletResponse badRequest = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(badRequest.getWriter()).thenReturn(writer);
        servlet.doPost(invalidNumber, badRequest);
        verify(badRequest).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("Errore: I campi 'Anno' e 'Durata' devono essere numeri validi.");

        doThrow(new IllegalStateException("service")).when(catalog)
                .addFilm(anyInt(), any(), anyInt(), any(), any(), any(), any(), any());
        HttpServletResponse serviceError = mock(HttpServletResponse.class);
        servlet.doPost(addFilmRequest(), serviceError);
        verify(serviceError).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Si è verificato un errore critico imprevisto.");
    }
}
