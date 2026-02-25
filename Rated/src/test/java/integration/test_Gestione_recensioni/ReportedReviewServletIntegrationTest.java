package integration.test_Gestione_recensioni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import integration.DatabaseSetupForTest;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Recensioni.view.ReportedReviewServlet;
import sottosistemi.Gestione_Utenti.service.ProfileService;

public class ReportedReviewServletIntegrationTest {

    private ReportedReviewServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    
    private CatalogoService catalogoService;
    private RecensioniService recensioniService;
    private ProfileService profileService;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB finto
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // A. Utente Moderatore
            String modSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('admin.mod@example.com', 'AdminMod', 'pwd', 'MODERATORE')";
            try(PreparedStatement ps = conn.prepareStatement(modSql)) { ps.executeUpdate(); }

            // B. Utente Recensore (Autore della recensione segnalata)
            String revSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES ('bad.boy@example.com', 'BadBoyUser', 'pwd', 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(revSql)) { ps.executeUpdate(); }

            // C. Film finto (ID = 555)
            String filmSql = "MERGE INTO Film (ID_Film, Nome, Anno, Durata, Regista) KEY(ID_Film) VALUES (555, 'Film Segnalato Test', 2024, 120, 'Regista Test')";
            try(PreparedStatement ps = conn.prepareStatement(filmSql)) { ps.executeUpdate(); }

            // D. Inseriamo la Recensione con N_Reports = 1, in modo che venga pescata da GetAllRecensioniSegnalate()
            String recSql = "MERGE INTO Recensione (email, ID_Film, Titolo, Contenuto, Valutazione, N_Reports) KEY(email, ID_Film) VALUES ('bad.boy@example.com', 555, 'Titolo Pessimo', 'Contenuto offensivo', 1, 1)";
            try(PreparedStatement ps = conn.prepareStatement(recSql)) { ps.executeUpdate(); }

            // E. Inseriamo la riga reale di segnalazione (Report) per correttezza del DB
            String repSql = "MERGE INTO Report (email, email_Recensore, ID_Film) KEY(email, email_Recensore, ID_Film) VALUES ('admin.mod@example.com', 'bad.boy@example.com', 555)";
            try(PreparedStatement ps = conn.prepareStatement(repSql)) { ps.executeUpdate(); }
        }

        // 2. Inizializzazione Service
        catalogoService = new CatalogoService();
        recensioniService = new RecensioniService();
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new ReportedReviewServlet();
        servlet.init();
        
        // 4. Iniezione multipla tramite Reflection (Attenzione ai nomi: la prima lettera è maiuscola)
        Field catField = ReportedReviewServlet.class.getDeclaredField("CatalogoService");
        catField.setAccessible(true);
        catField.set(servlet, catalogoService);
        
        Field recField = ReportedReviewServlet.class.getDeclaredField("RecensioniService");
        recField.setAccessible(true);
        recField.set(servlet, recensioniService);
        
        Field profField = ReportedReviewServlet.class.getDeclaredField("ProfileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        // 5. Setup dei Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/jsp/moderator.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReportedReview_Moderatore_Success_Integration() throws Exception {
        // --- ARRANGE ---
        
        // Simuliamo l'accesso del Moderatore
        UtenteBean user = new UtenteBean();
        user.setEmail("admin.mod@example.com");
        user.setTipoUtente("MODERATORE");
        when(session.getAttribute("user")).thenReturn(user);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Verifica List<RecensioneBean>
        ArgumentCaptor<List<RecensioneBean>> recensioniCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("recensioni"), recensioniCaptor.capture());
        List<RecensioneBean> recensioni = recensioniCaptor.getValue();
        
        assertNotNull(recensioni);
        assertFalse(recensioni.isEmpty(), "La lista delle recensioni segnalate non deve essere vuota");
        
        boolean recensioneTrovata = recensioni.stream()
                .anyMatch(r -> r.getEmail().equals("bad.boy@example.com") && r.getIdFilm() == 555);
        assertTrue(recensioneTrovata, "La recensione test 'bad.boy' dovrebbe essere presente");

        // 2. Verifica HashMap<String, String> Users
        ArgumentCaptor<HashMap<String, String>> usersCaptor = ArgumentCaptor.forClass((Class) HashMap.class);
        verify(session).setAttribute(eq("users"), usersCaptor.capture());
        HashMap<String, String> usersMap = usersCaptor.getValue();
        
        assertNotNull(usersMap);
        assertEquals("BadBoyUser", usersMap.get("bad.boy@example.com"), "La mappa utenti deve contenere lo username corretto dell'autore");

        // 3. Verifica HashMap<Integer, FilmBean> Films
        ArgumentCaptor<HashMap<Integer, FilmBean>> filmsCaptor = ArgumentCaptor.forClass((Class) HashMap.class);
        verify(session).setAttribute(eq("films"), filmsCaptor.capture());
        HashMap<Integer, FilmBean> filmsMap = filmsCaptor.getValue();
        
        assertNotNull(filmsMap);
        assertTrue(filmsMap.containsKey(555), "La mappa film deve contenere l'ID 555 del film segnalato");
        assertEquals("Film Segnalato Test", filmsMap.get(555).getNome());

        // 4. Verifica Forward JSP
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testReportedReview_NonModeratore_BadRequest() throws Exception {
        // --- ARRANGE ---
        
        // Utente standard (RECENSORE) prova ad accedere alla dashboard moderatori
        UtenteBean user = new UtenteBean();
        user.setTipoUtente("RECENSORE");
        when(session.getAttribute("user")).thenReturn(user);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // Verifica status code HTTP 400 (Bad Request come programmato nella tua servlet)
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("You can't access the profile page unless you are an authenticated moderator.");
        
        // Nessun interrogazione al DB deve essere stata fatta
        verify(session, never()).setAttribute(eq("recensioni"), any());
        verify(dispatcher, never()).forward(request, response);
    }
}