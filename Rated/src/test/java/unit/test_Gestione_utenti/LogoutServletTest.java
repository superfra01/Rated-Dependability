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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.view.LogoutServlet;

class LogoutServletTest {

    private static final class ExposedLogoutServlet extends LogoutServlet {
        void post(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            super.doPost(request, response);
        }
    }

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = LogoutServlet.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void logoutRedirectsAndToleratesCommittedOrBrokenResponse() throws Exception {
        AutenticationService service = mock(AutenticationService.class);
        LogoutServlet servlet = new LogoutServlet();
        inject(servlet, "authService", service);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/Rated");

        HttpServletResponse normal = mock(HttpServletResponse.class);
        servlet.doGet(request, normal);
        verify(normal).sendRedirect("/Rated/");

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);
        verify(committed, never()).sendRedirect(anyString());

        HttpServletResponse brokenRedirect = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(brokenRedirect).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, brokenRedirect));
    }

    @Test
    void serviceFailureHandlesAllErrorResponseStates() throws Exception {
        AutenticationService service = mock(AutenticationService.class);
        LogoutServlet servlet = new LogoutServlet();
        inject(servlet, "authService", service);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        doThrow(new IllegalStateException("logout")).when(service).logout(session);

        HttpServletResponse normal = mock(HttpServletResponse.class);
        servlet.doGet(request, normal);
        verify(normal).sendError(anyInt(), anyString());

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, broken));
    }

    @Test
    void postDelegatesToGet() throws Exception {
        ExposedLogoutServlet servlet = new ExposedLogoutServlet();
        servlet.post(mock(HttpServletRequest.class), mock(HttpServletResponse.class));
    }

    @Test
    void postHandlesCheckedDelegationFailure() throws Exception {
        class FailingLogoutServlet extends LogoutServlet {
            @Override
            public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
                throw new ServletException("delegation");
            }

            void post(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                super.doPost(request, response);
            }
        }

        HttpServletResponse response = mock(HttpServletResponse.class);
        new FailingLogoutServlet().post(mock(HttpServletRequest.class), response);
        verify(response).sendError(anyInt(), anyString());
    }
}
