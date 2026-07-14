package unit.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.ModificaFilmServlet;

class ModificaFilmServletTest {

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
        user.setTipoUtente(role);
        return user;
    }

    private static HttpServletRequest request(UtenteBean user) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(true)).thenReturn(session);
        when(request.getParameter("idFilm")).thenReturn("7");
        when(request.getParameter("annoFilm")).thenReturn("2024");
        when(request.getParameter("durataFilm")).thenReturn("120");
        when(request.getParameter("nomeFilm")).thenReturn("Film");
        when(request.getContextPath()).thenReturn("/Rated");
        return request;
    }

    @Test
    void rejectsMissingSessionAndWrongRoleIncludingWriterFailure() throws Exception {
        ModificaFilmServlet servlet = new ModificaFilmServlet();
        HttpServletRequest noSession = mock(HttpServletRequest.class);
        when(noSession.getSession(true)).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        servlet.doPost(noSession, response);
        verify(response).setStatus(401);

        HttpServletRequest wrongRole = request(user("UTENTE"));
        HttpServletResponse broken = mock(HttpServletResponse.class);
        when(broken.getWriter()).thenThrow(new IOException("closed"));
        assertDoesNotThrow(() -> servlet.doPost(wrongRole, broken));
    }

    @Test
    void validatesIdAndProcessesAllPosterCases() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        ModificaFilmServlet servlet = new ModificaFilmServlet();
        inject(servlet, "catalogoService", service);

        HttpServletRequest missingId = request(user("GESTORE"));
        when(missingId.getParameter("idFilm")).thenReturn(null);
        HttpServletResponse badRequest = mock(HttpServletResponse.class);
        servlet.doPost(missingId, badRequest);
        verify(badRequest).setStatus(400);

        HttpServletRequest noPart = request(user("GESTORE"));
        servlet.doPost(noPart, mock(HttpServletResponse.class));
        verify(service).modifyFilm(eq(7), eq(2024), any(), eq(120), any(), eq(null), eq("Film"), any(), any());

        HttpServletRequest emptyPartRequest = request(user("GESTORE"));
        Part emptyPart = mock(Part.class);
        when(emptyPartRequest.getPart("locandinaFilm")).thenReturn(emptyPart);
        servlet.doPost(emptyPartRequest, mock(HttpServletResponse.class));

        HttpServletRequest posterRequest = request(user("GESTORE"));
        Part poster = mock(Part.class);
        when(poster.getSize()).thenReturn(2L);
        when(poster.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2}));
        when(posterRequest.getPart("locandinaFilm")).thenReturn(poster);
        when(posterRequest.getContextPath()).thenReturn(null);
        HttpServletResponse posterResponse = mock(HttpServletResponse.class);
        servlet.doPost(posterRequest, posterResponse);
        verify(posterResponse).sendRedirect("/film?idFilm=7");
    }

    @Test
    void handlesCommittedRedirectAndOuterFailures() throws Exception {
        CatalogoService service = mock(CatalogoService.class);
        ModificaFilmServlet servlet = new ModificaFilmServlet();
        inject(servlet, "catalogoService", service);

        HttpServletRequest committedRequest = request(user("GESTORE"));
        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        servlet.doPost(committedRequest, committed);
        verify(committed, never()).sendRedirect(anyString());

        HttpServletRequest redirectRequest = request(user("GESTORE"));
        HttpServletResponse redirectFailure = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(redirectFailure).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doPost(redirectRequest, redirectFailure));

        HttpServletRequest invalidNumber = request(user("GESTORE"));
        when(invalidNumber.getParameter("annoFilm")).thenReturn("bad");
        HttpServletResponse error = mock(HttpServletResponse.class);
        servlet.doPost(invalidNumber, error);
        verify(error).sendError(500, "Errore imprevisto nel sistema.");

        HttpServletResponse committedError = mock(HttpServletResponse.class);
        when(committedError.isCommitted()).thenReturn(true);
        servlet.doPost(invalidNumber, committedError);

        HttpServletResponse brokenError = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(brokenError).sendError(anyInt(), anyString());
        assertDoesNotThrow(() -> servlet.doPost(invalidNumber, brokenError));
    }

    @Test
    void getHandlesRedirectFailure() throws Exception {
        ModificaFilmServlet servlet = new ModificaFilmServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/Rated");
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IOException("closed")).when(response).sendRedirect(anyString());
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void getRedirectAndPosterConditionsHaveObservableResults() throws Exception {
        ModificaFilmServlet servlet = new ModificaFilmServlet();
        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getContextPath()).thenReturn("/Rated");
        HttpServletResponse getResponse = mock(HttpServletResponse.class);
        servlet.doGet(get, getResponse);
        verify(getResponse).sendRedirect("/Rated/catalogo");

        CatalogoService service = mock(CatalogoService.class);
        inject(servlet, "catalogoService", service);
        HttpServletRequest missing = request(user("GESTORE"));
        servlet.doPost(missing, mock(HttpServletResponse.class));

        HttpServletRequest empty = request(user("GESTORE"));
        Part emptyPart = mock(Part.class);
        when(empty.getPart("locandinaFilm")).thenReturn(emptyPart);
        servlet.doPost(empty, mock(HttpServletResponse.class));

        HttpServletRequest present = request(user("GESTORE"));
        Part presentPart = mock(Part.class);
        when(presentPart.getSize()).thenReturn(1L);
        when(presentPart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {9}));
        when(present.getPart("locandinaFilm")).thenReturn(presentPart);
        servlet.doPost(present, mock(HttpServletResponse.class));

        ArgumentCaptor<byte[]> posters = ArgumentCaptor.forClass(byte[].class);
        verify(service, org.mockito.Mockito.times(3)).modifyFilm(eq(7), eq(2024), any(), eq(120),
                any(), posters.capture(), eq("Film"), any(), any());
        org.junit.jupiter.api.Assertions.assertNull(posters.getAllValues().get(0));
        org.junit.jupiter.api.Assertions.assertNull(posters.getAllValues().get(1));
        org.junit.jupiter.api.Assertions.assertArrayEquals(new byte[] {9}, posters.getAllValues().get(2));
    }
}
