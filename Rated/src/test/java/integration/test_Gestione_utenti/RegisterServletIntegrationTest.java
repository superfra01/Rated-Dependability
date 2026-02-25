package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import integration.DatabaseSetupForTest;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.RegisterServlet;

public class RegisterServletIntegrationTest {

    private RegisterServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private PrintWriter writer;
    private Part filePart;
    private DataSource dataSource;
    
    private AutenticationService authService;
    private ProfileService profService;
    private CatalogoService catalogoService;

    // Dati per l'utente da registrare
    private static final String NEW_EMAIL = "nuovo.utente@example.com";
    private static final String NEW_USERNAME = "NuovoUser123";
    private static final String VALID_PASSWORD = "ValidPassword123!";

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            // Inseriamo i generi nel DB per evitare eccezioni di Foreign Key quando si salvano le preferenze
            String gen1 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Azione')";
            String gen2 = "MERGE INTO Genere (Nome) KEY(Nome) VALUES ('Fantascienza')";
            try(PreparedStatement ps = conn.prepareStatement(gen1)) { ps.executeUpdate(); }
            try(PreparedStatement ps = conn.prepareStatement(gen2)) { ps.executeUpdate(); }
            
            // Assicuriamoci che l'utente non esista già per non far fallire la registrazione
            String cleanSql = "DELETE FROM Utente_Registrato WHERE email = ?";
            try(PreparedStatement ps = conn.prepareStatement(cleanSql)) { 
                ps.setString(1, NEW_EMAIL);
                ps.executeUpdate(); 
            }
        }

        // 2. Inizializzazione dei Service reali
        authService = new AutenticationService();
        profService = new ProfileService();
        catalogoService = new CatalogoService();

        // 3. Inizializzazione Servlet
        servlet = new RegisterServlet();
        servlet.init();
        
        // 4. Iniezione multipla delle dipendenze
        Field authField = RegisterServlet.class.getDeclaredField("authService");
        authField.setAccessible(true);
        authField.set(servlet, authService);
        
        Field profField = RegisterServlet.class.getDeclaredField("profService");
        profField.setAccessible(true);
        profField.set(servlet, profService);
        
        Field catField = RegisterServlet.class.getDeclaredField("catalogoService");
        catField.setAccessible(true);
        catField.set(servlet, catalogoService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        writer = mock(PrintWriter.class);
        filePart = mock(Part.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/Rated");
        when(response.getWriter()).thenReturn(writer);
        when(request.getRequestDispatcher("/WEB-INF/jsp/register.jsp")).thenReturn(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoGet_CaricamentoGeneri() throws Exception {
        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        ArgumentCaptor<List<String>> generiCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(session).setAttribute(eq("genres"), generiCaptor.capture());
        
        List<String> generiCaricati = generiCaptor.getValue();
        assertNotNull(generiCaricati);
        assertTrue(generiCaricati.contains("Azione") && generiCaricati.contains("Fantascienza"), 
                "I generi inseriti nel setup devono essere estratti dal DB");
        
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testRegister_Success_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("username")).thenReturn(NEW_USERNAME);
        when(request.getParameter("email")).thenReturn(NEW_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);
        when(request.getParameter("confirm_password")).thenReturn(VALID_PASSWORD); // Le password coincidono
        when(request.getParameter("biography")).thenReturn("Biografia del nuovo utente");
        when(request.getParameterValues("genres")).thenReturn(new String[]{"Azione", "Fantascienza"});
        
        // Simuliamo l'upload dell'icona
        when(request.getPart("profile_icon")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(100L);
        InputStream is = new ByteArrayInputStream("byte_icona_test".getBytes());
        when(filePart.getInputStream()).thenReturn(is);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica Redirect al login
        verify(response).sendRedirect("/Rated/login");

        // 2. Verifica DB: L'utente deve essere presente in Utente_Registrato
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Utente_Registrato WHERE email = ?")) {
            ps.setString(1, NEW_EMAIL);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "L'utente deve essere stato inserito nel Database");
            assertEquals(NEW_USERNAME, rs.getString("username"));
            assertEquals("Biografia del nuovo utente", rs.getString("Biografia"));
            assertNotNull(rs.getBytes("Icona"), "L'icona non deve essere nulla");
        }
        
        // 3. Verifica DB: Le preferenze devono essere presenti
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Preferenza WHERE email = ?")) {
            ps.setString(1, NEW_EMAIL);
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(2, rs.getInt(1), "Devono essere state salvate 2 preferenze di generi");
        }
    }

    @Test
    public void testRegister_PasswordMismatch_BadRequest() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("username")).thenReturn(NEW_USERNAME);
        when(request.getParameter("email")).thenReturn(NEW_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);
        when(request.getParameter("confirm_password")).thenReturn("PasswordDiversa123!"); // MISMATCH

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("Invalid form data. Check your inputs.");
    }
    
    @Test
    public void testRegister_InvalidEmail_BadRequest() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("username")).thenReturn(NEW_USERNAME);
        when(request.getParameter("email")).thenReturn("email_non_valida"); // Errata
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);
        when(request.getParameter("confirm_password")).thenReturn(VALID_PASSWORD); 

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(writer).write("Invalid form data. Check your inputs.");
    }
}