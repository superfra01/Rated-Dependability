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

class RegisterServletTest {

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
    void registerGetHandlesForwardCommittedAndSendErrorFailure() throws Exception {
        CatalogoService catalog = mock(CatalogoService.class);
        RegisterServlet servlet = new RegisterServlet();
        inject(servlet, "catalogoService", catalog);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/WEB-INF/jsp/register.jsp")).thenReturn(dispatcher);
        doThrow(new ServletException("forward")).when(dispatcher).forward(any(), any());

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, broken));

        HttpServletResponse normalError = mock(HttpServletResponse.class);
        servlet.doGet(request, normalError);
        verify(normalError).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Si è verificato un errore durante il caricamento della pagina di registrazione.");
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
    void registerRejectsEachInvalidFormConditionAndWriterFailure() throws Exception {
        RegisterServlet servlet = new RegisterServlet();
        String[][] invalid = {
                {"x", "valid@example.com", "Pippo1234.", "Pippo1234."},
                {"validUser", "bad", "Pippo1234.", "Pippo1234."},
                {"validUser", "valid@example.com", "weak", "weak"},
                {"validUser", "valid@example.com", "Pippo1234.", "Different1."}
        };
        for (String[] values : invalid) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(response.getWriter()).thenReturn(mock(PrintWriter.class));
            servlet.doPost(registrationRequest(values[0], values[1], values[2], values[3]), response);
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        HttpServletResponse broken = mock(HttpServletResponse.class);
        when(broken.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(registrationRequest("x", "bad", "weak", "no"), broken));
    }

    @Test
    void registerSuccessReadsIconAddsGenresAndRedirects() throws Exception {
        AutenticationService auth = mock(AutenticationService.class);
        ProfileService profile = mock(ProfileService.class);
        RegisterServlet servlet = new RegisterServlet();
        inject(servlet, "authService", auth);
        inject(servlet, "profService", profile);
        HttpServletRequest request = registrationRequest("validUser", "valid@example.com", "Pippo1234.", "Pippo1234.");
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(3L);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2, 3}));
        when(request.getPart("profile_icon")).thenReturn(part);
        when(request.getParameterValues("genres")).thenReturn(new String[] {"Drama", "Comedy"});
        when(request.getContextPath()).thenReturn("/rated");
        when(auth.register(eq("validUser"), eq("valid@example.com"), eq("Pippo1234."), eq(null), any(byte[].class)))
                .thenReturn(user("UTENTE"));
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.doPost(request, response);

        verify(profile).addPreferenza("valid@example.com", "Drama");
        verify(profile).addPreferenza("valid@example.com", "Comedy");
        verify(response).sendRedirect("/rated/login");
    }

    @Test
    void registerHandlesDuplicateRedirectWriterAndGlobalFailures() throws Exception {
        AutenticationService auth = mock(AutenticationService.class);
        RegisterServlet servlet = new RegisterServlet();
        inject(servlet, "authService", auth);
        HttpServletRequest request = registrationRequest("validUser", "valid@example.com", "Pippo1234.", "Pippo1234.");
        HttpServletResponse duplicate = mock(HttpServletResponse.class);
        PrintWriter duplicateWriter = mock(PrintWriter.class);
        when(duplicate.getWriter()).thenReturn(duplicateWriter);
        servlet.doPost(request, duplicate);
        verify(duplicate).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(duplicateWriter).write("Registration failed. User may already exist.");

        HttpServletResponse writerFailure = mock(HttpServletResponse.class);
        when(writerFailure.getWriter()).thenThrow(new IOException("closed"));
        servlet.doPost(request, writerFailure);

        when(auth.register(anyString(), anyString(), anyString(), eq(null), eq(null))).thenReturn(user("UTENTE"));
        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect("/login");
        servlet.doPost(request, redirectFailure);

        HttpServletRequest global = mock(HttpServletRequest.class);
        when(global.getParameter("username")).thenThrow(new IllegalStateException("request"));
        HttpServletResponse normalGlobal = mock(HttpServletResponse.class);
        servlet.doPost(global, normalGlobal);
        verify(normalGlobal).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Si è verificato un errore durante la procedura di registrazione.");

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(global, broken));

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(global, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        Part emptyPart = mock(Part.class);
        HttpServletRequest emptyIcon = registrationRequest("validUser", "valid@example.com", "Pippo1234.", "Pippo1234.");
        when(emptyIcon.getPart("profile_icon")).thenReturn(emptyPart);
        servlet.doPost(emptyIcon, mock(HttpServletResponse.class));
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

}
