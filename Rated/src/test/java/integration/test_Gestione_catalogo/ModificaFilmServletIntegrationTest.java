package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Catalogo.view.ModificaFilmServlet;

public class ModificaFilmServletIntegrationTest {

    private ModificaFilmServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private Part filePart;
    private DataSource dataSource;
    private CatalogoService catalogoService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Configurazione del Database di test (H2) inizializzato tramite init.sql
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        try (Connection conn = dataSource.getConnection()) {
            String insertGenere = "MERGE INTO Genere (Nome) KEY(Nome) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insertGenere)) {
                String[] generiDaTestare = {"Fantascienza", "Thriller"};
                for (String g : generiDaTestare) {
                    ps.setString(1, g);
                    ps.executeUpdate();
                }
            }
        }
        
        FilmDAO filmDAO = new FilmDAO(dataSource);
        FilmGenereDAO filmGenereDAO = new FilmGenereDAO(dataSource);
        GenereDAO genereDAO = new GenereDAO(dataSource);
        catalogoService = new CatalogoService(filmDAO,  filmGenereDAO, genereDAO );

        // 2. Inizializzazione Servlet
        servlet = new ModificaFilmServlet();
        servlet.init();
        
        // 3. Iniezione della dipendenza tramite Reflection
        Field serviceField = ModificaFilmServlet.class.getDeclaredField("CatalogoService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, catalogoService);

        // 4. Mock degli oggetti della Servlet API
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        filePart = mock(Part.class);

        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testModificaFilmSuccess_Integration() throws Exception {
        // --- ARRANGE ---
        
        // 1. Mock dell'utente in sessione con ruolo "GESTORE" (es: gestore@catalogo.it da init.sql)
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("GESTORE");
        user.setEmail("gestore@catalogo.it");
        when(session.getAttribute("user")).thenReturn(user);

        // 2. Mock dei parametri per modificare il Film con ID = 1 ("Inception" nel DB originale)
        int idDaModificare = 1;
        when(request.getParameter("idFilm")).thenReturn(String.valueOf(idDaModificare));
        when(request.getParameter("annoFilm")).thenReturn("2025"); // Modifichiamo l'anno da 2010 a 2025
        when(request.getParameter("attoriFilm")).thenReturn("Leonardo DiCaprio, Nuovo Attore"); 
        when(request.getParameter("durataFilm")).thenReturn("150"); // Modifichiamo la durata da 148 a 150
        
        // Assegniamo nuovi generi validi (devono esistere nella tabella Genere di init.sql)
        when(request.getParameterValues("generiFilm")).thenReturn(new String[]{"Fantascienza", "Thriller"});
        
        when(request.getParameter("nomeFilm")).thenReturn("Inception - Director's Cut");
        when(request.getParameter("registaFilm")).thenReturn("Christopher Nolan");
        when(request.getParameter("tramaFilm")).thenReturn("Trama aggiornata tramite Integration Test.");

        // 3. Mock del caricamento del file "locandinaFilm"
        when(request.getPart("locandinaFilm")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(200L);
        InputStream is = new ByteArrayInputStream("nuova locandina finta".getBytes());
        when(filePart.getInputStream()).thenReturn(is);

        // Mock context path
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica HTTP: Assicuriamoci che abbia effettuato il redirect alla pagina del film
        verify(response).sendRedirect("/Rated/film?idFilm=" + idDaModificare);

        // 2. Verifica Database: Recuperiamo il film per assicurarci che sia stato davvero modificato
        List<FilmBean> risultatoRicerca = catalogoService.ricercaFilm("Inception - Director's Cut");
        
        FilmBean filmAggiornato = risultatoRicerca.stream()
                .filter(f -> f.getIdFilm() == idDaModificare)
                .findFirst()
                .orElse(null);

        assertNotNull(filmAggiornato, "Il film modificato deve esistere nel Database.");
        assertEquals(2025, filmAggiornato.getAnno(), "L'anno deve essere stato aggiornato a 2025.");
        assertEquals(150, filmAggiornato.getDurata(), "La durata deve essere stata aggiornata a 150.");
        assertEquals("Inception - Director's Cut", filmAggiornato.getNome(), "Il nome deve corrispondere.");
        assertEquals("Trama aggiornata tramite Integration Test.", filmAggiornato.getTrama(), "La trama deve essere stata modificata.");
    }

    @Test
    public void testModificaFilmUnauthorized() throws Exception {
        // --- ARRANGE ---
        
        // Utente con permessi "RECENSORE" (es. alice.rossi@example.com da init.sql)
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("RECENSORE");
        user.setEmail("alice.rossi@example.com");
        when(session.getAttribute("user")).thenReturn(user);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // Deve bloccare l'accesso (Status 401)
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Non hai i permessi per effettuare la seguente operazione");
        
        // Assicuriamoci che non abbia tentato di leggere o manipolare dati
        verify(request, never()).getParameter("idFilm");
        verify(response, never()).sendRedirect(anyString());
    }
}