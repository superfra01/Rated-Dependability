package unit.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.view.LoginServlet;

class LoginServletTest {

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static HttpServletRequest request(String email, String password) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(mock(RequestDispatcher.class));
        return request;
    }

    @Test
    void rejectsEveryInvalidCredentialShape() throws Exception {
        LoginServlet servlet = new LoginServlet();
        for (String[] credentials : new String[][] {
                {null, "Pippo1234."}, {"user@example.com", null}, {"bad-email", "Pippo1234."},
                {"user@example.com", "weak"}}) {
            HttpServletRequest request = request(credentials[0], credentials[1]);
            HttpServletResponse response = mock(HttpServletResponse.class);
            servlet.doPost(request, response);
            verify(request).setAttribute("loginError", "Errore di LogIn");
        }
    }

    @Test
    void handlesFailedSuccessfulAndExceptionalLogin() throws Exception {
        AutenticationService service = mock(AutenticationService.class);
        LoginServlet servlet = new LoginServlet();
        inject(servlet, "authService", service);

        HttpServletRequest failed = request("user@example.com", "Pippo1234.");
        servlet.doPost(failed, mock(HttpServletResponse.class));
        verify(failed).setAttribute("loginError", "Email o password non valide.");

        UtenteBean user = new UtenteBean();
        user.setUsername("tester");
        when(service.login("user@example.com", "Pippo1234.")).thenReturn(user);
        HttpServletRequest successful = request("user@example.com", "Pippo1234.");
        HttpSession session = mock(HttpSession.class);
        when(successful.getSession(true)).thenReturn(session);
        when(successful.getContextPath()).thenReturn(null);
        HttpServletResponse successfulResponse = mock(HttpServletResponse.class);
        servlet.doPost(successful, successfulResponse);
        verify(session).setAttribute("user", user);
        verify(successfulResponse).sendRedirect("/");

        when(service.login("other@example.com", "Pippo1234.")).thenThrow(new IllegalStateException("db"));
        HttpServletResponse error = mock(HttpServletResponse.class);
        servlet.doPost(request("other@example.com", "Pippo1234."), error);
        verify(error).sendError(anyInt(), anyString());

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request("other@example.com", "Pippo1234."), committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(request("other@example.com", "Pippo1234."), broken));
    }

    @Test
    void safeForwardAndGetHandleCommittedAndBrokenDispatchers() throws Exception {
        LoginServlet servlet = new LoginServlet();
        HttpServletRequest request = request("bad", "bad");
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request, committed);
        verify(request, never()).setAttribute(anyString(), anyString());

        HttpServletRequest brokenForward = request("bad", "bad");
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(brokenForward.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);
        doThrow(new ServletException("forward")).when(dispatcher).forward(brokenForward, committed);
        assertDoesNotThrow(() -> servlet.doPost(brokenForward, committed));

        HttpServletRequest brokenGet = mock(HttpServletRequest.class);
        when(brokenGet.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        doThrow(new IOException("forward")).when(dispatcher).forward(brokenGet, getResponse);
        servlet.doGet(brokenGet, getResponse);
        verify(getResponse).sendError(anyInt(), anyString());
    }
}
