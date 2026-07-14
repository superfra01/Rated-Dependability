package unit.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.ProfileModifyServlet;

class ProfileModifyServletTest {

    @BeforeEach
    void initializeDataSource() {
        DatabaseSetupForTest.getH2DataSource();
    }

    private static void inject(Object target, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField("ProfileService");
        field.setAccessible(true);
        field.set(target, value);
    }

    private static HttpServletRequest request(String username, String password, Part icon) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("email")).thenReturn("user@example.com");
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("biography")).thenReturn("bio");
        when(request.getPart("icon")).thenReturn(icon);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));
        when(request.getContextPath()).thenReturn("/Rated");
        return request;
    }

    @Test
    void validatesInputAndSupportsMissingEmptyAndPresentIcon() throws Exception {
        ProfileService service = mock(ProfileService.class);
        ProfileModifyServlet servlet = new ProfileModifyServlet();
        inject(servlet, service);

        servlet.doPost(request("bad name", "Pippo1234.", null), mock(HttpServletResponse.class));
        servlet.doPost(request("tester", "weak", null), mock(HttpServletResponse.class));

        Part empty = mock(Part.class);
        servlet.doPost(request("tester", "Pippo1234.", empty), mock(HttpServletResponse.class));

        Part icon = mock(Part.class);
        when(icon.getSize()).thenReturn(2L);
        when(icon.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2}));
        servlet.doPost(request("tester", "Pippo1234.", icon), mock(HttpServletResponse.class));
        verify(service).ProfileUpdate("tester", "user@example.com", "Pippo1234.", "bio", new byte[] {1, 2});
    }

    @Test
    void redirectsForBothUpdateResultsAndHandlesRedirectFailure() throws Exception {
        ProfileService service = mock(ProfileService.class);
        ProfileModifyServlet servlet = new ProfileModifyServlet();
        inject(servlet, service);

        HttpServletResponse missing = mock(HttpServletResponse.class);
        servlet.doPost(request("tester", "Pippo1234.", null), missing);
        verify(missing).sendRedirect("/Rated/");

        UtenteBean user = new UtenteBean();
        user.setUsername("tester");
        when(service.ProfileUpdate(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.<byte[]>isNull()))
                .thenReturn(user);
        HttpServletRequest updatedRequest = request("tester", "Pippo1234.", null);
        HttpSession session = updatedRequest.getSession(true);
        HttpServletResponse updated = mock(HttpServletResponse.class);
        servlet.doPost(updatedRequest, updated);
        verify(session).setAttribute("user", user);
        verify(updated).sendRedirect("/Rated/profile?visitedUser=tester");

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doPost(request("tester", "Pippo1234.", null), broken));
    }

    @Test
    void handlesMultipartAndErrorResponseFailures() throws Exception {
        ProfileModifyServlet servlet = new ProfileModifyServlet();
        HttpServletRequest brokenPart = mock(HttpServletRequest.class);
        when(brokenPart.getPart("icon")).thenThrow(new ServletException("part"));

        HttpServletResponse normal = mock(HttpServletResponse.class);
        servlet.doPost(brokenPart, normal);
        verify(normal).sendError(anyInt(), anyString());

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(brokenPart, committed);
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(brokenPart, broken));
    }

    @Test
    void getEntryPointIsCovered() throws Exception {
        new ProfileModifyServlet().doGet(mock(HttpServletRequest.class), mock(HttpServletResponse.class));
    }
}
