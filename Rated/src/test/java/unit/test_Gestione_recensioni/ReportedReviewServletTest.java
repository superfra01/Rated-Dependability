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

class ReportedReviewServletTest {

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
    void reportedReviewCoversUnauthorizedSuccessAndGetFailures() throws Exception {
        RecensioniService reviews = mock(RecensioniService.class);
        ProfileService profile = mock(ProfileService.class);
        CatalogoService catalog = mock(CatalogoService.class);
        ReportedReviewServlet servlet = new ReportedReviewServlet();
        inject(servlet, "RecensioniService", reviews);
        inject(servlet, "ProfileService", profile);
        inject(servlet, "CatalogoService", catalog);

        HttpServletRequest anonymous = mock(HttpServletRequest.class);
        HttpSession anonymousSession = session(null);
        when(anonymous.getSession(true)).thenReturn(anonymousSession);
        HttpServletResponse unauthorized = writableResponse();
        servlet.doGet(anonymous, unauthorized);
        verify(unauthorized).setStatus(400);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession moderator = session(user("MODERATORE"));
        when(request.getSession(true)).thenReturn(moderator);
        when(reviews.GetAllRecensioniSegnalate()).thenReturn(Collections.emptyList());
        when(profile.getUsers(Collections.emptyList())).thenReturn(new HashMap<>());
        when(catalog.getFilms(Collections.emptyList())).thenReturn(new HashMap<>());
        when(request.getRequestDispatcher("/WEB-INF/jsp/moderator.jsp")).thenReturn(mock(RequestDispatcher.class));
        servlet.doGet(request, mock(HttpServletResponse.class));

        when(reviews.GetAllRecensioniSegnalate()).thenThrow(new IllegalStateException("service"));
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doGet(request, broken);
    }

    @Test
    void reportedReviewPostCoversDelegationFailureCommittedAndSendErrorIo() throws Exception {
        ReportedReviewServlet servlet = spy(new ReportedReviewServlet());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse broken = mock(HttpServletResponse.class);
        doThrow(new IOException("delegation")).when(servlet).doGet(request, broken);
        doThrow(new IOException("closed")).when(broken).sendError(anyInt(), anyString());
        servlet.doPost(request, broken);

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        doThrow(new ServletException("delegation")).when(servlet).doGet(request, committed);
        servlet.doPost(request, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }

    @Test
    void reportedReviewCoversNullSessionAndCommittedGetFailure() throws Exception {
        ReportedReviewServlet servlet = new ReportedReviewServlet();
        HttpServletRequest noSession = mock(HttpServletRequest.class);
        when(noSession.getSession(true)).thenReturn(null);
        servlet.doGet(noSession, writableResponse());

        RecensioniService reviews = mock(RecensioniService.class);
        inject(servlet, "RecensioniService", reviews);
        when(reviews.GetAllRecensioniSegnalate()).thenThrow(new IllegalStateException("service"));
        HttpServletRequest moderator = mock(HttpServletRequest.class);
        HttpSession moderatorSession = session(user("MODERATORE"));
        when(moderator.getSession(true)).thenReturn(moderatorSession);
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doGet(moderator, committed);
        verify(committed, never()).sendError(anyInt(), anyString());
    }
    @Test
    void reportedReviewCoversNormalPostAndBothErrorWriters() throws Exception {
        ReportedReviewServlet normal = spy(new ReportedReviewServlet());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        org.mockito.Mockito.doNothing().when(normal).doGet(request, response);
        normal.doPost(request, response);
        verify(normal).doGet(request, response);

        ReportedReviewServlet failing = new ReportedReviewServlet() {
            @Override
            public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
                throw new ServletException("delegation");
            }
        };
        HttpServletResponse brokenPost = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(brokenPost).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> failing.doPost(request, brokenPost));

        HttpServletRequest brokenGet = mock(HttpServletRequest.class);
        when(brokenGet.getSession(true)).thenThrow(new IllegalStateException("session"));
        HttpServletResponse brokenGetResponse = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(brokenGetResponse).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> new ReportedReviewServlet().doGet(brokenGet, brokenGetResponse));

        HttpServletResponse normalError = mock(HttpServletResponse.class);
        new ReportedReviewServlet().doGet(brokenGet, normalError);
        verify(normalError).sendError(anyInt(), anyString());
    }
}
