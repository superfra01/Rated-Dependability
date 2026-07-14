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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.PasswordModifyServlet;

class PasswordModifyServletTest {

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField("ProfileService");
        field.setAccessible(true);
        field.set(target, value);
    }

    private static HttpServletRequest request(String password) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("email")).thenReturn("user@example.com");
        when(request.getParameter("password")).thenReturn(password);
        when(request.getContextPath()).thenReturn("/Rated");
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));
        return request;
    }

    @Test
    void coversInvalidPasswordAndBothUpdateResults() throws Exception {
        ProfileService service = mock(ProfileService.class);
        PasswordModifyServlet servlet = new PasswordModifyServlet();
        inject(servlet, service);

        HttpServletResponse invalid = mock(HttpServletResponse.class);
        servlet.doPost(request("weak"), invalid);
        verify(invalid).sendRedirect("/Rated/profile?error=invalidPassword");

        HttpServletResponse notUpdated = mock(HttpServletResponse.class);
        servlet.doPost(request("Pippo1234."), notUpdated);
        verify(notUpdated).sendRedirect("/Rated/");

        UtenteBean user = new UtenteBean();
        user.setUsername("tester");
        when(service.PasswordUpdate("user@example.com", "Pippo1234.")).thenReturn(user);
        HttpServletResponse updated = mock(HttpServletResponse.class);
        servlet.doPost(request("Pippo1234."), updated);
        verify(updated).sendRedirect("/Rated/profile?visitedUser=tester");
    }

    @Test
    void coversRedirectAndSystemFailures() throws Exception {
        ProfileService service = mock(ProfileService.class);
        PasswordModifyServlet servlet = new PasswordModifyServlet();
        inject(servlet, service);
        UtenteBean user = new UtenteBean();
        user.setUsername("tester");
        when(service.PasswordUpdate("user@example.com", "Pippo1234.")).thenReturn(user);

        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doPost(request("Pippo1234."), redirectFailure));
        assertDoesNotThrow(() -> servlet.doPost(request("weak"), redirectFailure));

        when(service.PasswordUpdate("user@example.com", "Pippo1234.")).thenThrow(new IllegalStateException("db"));
        HttpServletResponse error = mock(HttpServletResponse.class);
        servlet.doPost(request("Pippo1234."), error);
        verify(error).sendError(anyInt(), anyString());

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(request("Pippo1234."), committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(request("Pippo1234."), broken));
    }

    @Test
    void getEntryPointIsCovered() throws Exception {
        new PasswordModifyServlet().doGet(mock(HttpServletRequest.class), mock(HttpServletResponse.class));
    }
}
