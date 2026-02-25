package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Utenti.view.ProfileModifyServlet;

public class ProfileModifyServletIntegrationTest {

    private ProfileModifyServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private Part filePart;
    private DataSource dataSource;
    
    private ProfileService profileService;

    // Dati costanti per isolare il test
    private static final String TARGET_EMAIL = "profile.update@example.com";
    private static final String VALID_USERNAME = "NuovoUser123";
    private static final String VALID_PASSWORD = "ValidPassword123!";

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            // Inseriamo l'utente bersaglio prima della modifica
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente, Biografia) KEY(email) VALUES (?, 'VecchioUsername', 'VecchiaPwd123!', 'RECENSORE', 'Vecchia Bio')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { 
                ps.setString(1, TARGET_EMAIL);
                ps.executeUpdate(); 
            }
        }

        // 2. Inizializzazione Service
        profileService = new ProfileService();

        // 3. Inizializzazione Servlet
        servlet = new ProfileModifyServlet();
        servlet.init();
        
        // 4. Iniezione del Service (la variabile in ProfileModifyServlet ha la "P" maiuscola: "ProfileService")
        Field profField = ProfileModifyServlet.class.getDeclaredField("ProfileService");
        profField.setAccessible(true);
        profField.set(servlet, profileService);

        // 5. Mock HTTP e Componenti Servlet
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        filePart = mock(Part.class);

        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/Rated");

        // 6. Simula il comportamento reale di una HttpSession (come una HashMap)
        Map<String, Object> sessionAttributes = new HashMap<>();
        
        doAnswer(invocation -> {
            sessionAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());

        when(session.getAttribute(anyString())).thenAnswer(invocation -> {
            return sessionAttributes.get(invocation.getArgument(0));
        });
    }

    @Test
    public void testProfileModify_Success_Integration() throws Exception {
        // --- ARRANGE ---
        // Parametri testuali (convalidati positivamente dal FieldValidator)
        when(request.getParameter("email")).thenReturn(TARGET_EMAIL);
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);
        when(request.getParameter("biography")).thenReturn("Questa è la mia nuova biografia aggiornata.");

        // Simuliamo l'upload di un'immagine (Icona del profilo)
        when(request.getPart("icon")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(150L);
        InputStream is = new ByteArrayInputStream("finti_byte_immagine".getBytes());
        when(filePart.getInputStream()).thenReturn(is);

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Verifica HTTP: deve aver aggiornato la sessione con i due attributi e fatto il redirect
        verify(session).setAttribute(eq("user"), any(UtenteBean.class));
        verify(session).setAttribute(eq("visitedUser"), any(UtenteBean.class));
        
        // Controlliamo che il redirect abbia usato il nuovo username appena salvato!
        verify(response).sendRedirect("/Rated/profile?visitedUser=" + VALID_USERNAME);

        // 2. Verifica Database: L'aggiornamento deve essere effettivamente atterrato su DB H2
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT username, Biografia, Icona FROM Utente_Registrato WHERE email = ?")) {
            
            ps.setString(1, TARGET_EMAIL);
            ResultSet rs = ps.executeQuery();
            rs.next();
            
            String dbUsername = rs.getString("username");
            String dbBio = rs.getString("Biografia");
            byte[] dbIcon = rs.getBytes("Icona");
            
            // Verifichiamo la persistenza corretta
            assertEquals(VALID_USERNAME, dbUsername, "L'username nel DB dovrebbe essere stato aggiornato");
            assertEquals("Questa è la mia nuova biografia aggiornata.", dbBio, "La biografia nel DB dovrebbe essere aggiornata");
            assertNotNull(dbIcon, "I byte dell'icona dovrebbero essere stati salvati nel DB");
            assertEquals("finti_byte_immagine".length(), dbIcon.length, "La lunghezza dei byte dell'icona deve coincidere");
        }
    }

    @Test
    public void testProfileModify_InvalidFormat_NoAction() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("email")).thenReturn(TARGET_EMAIL);
        
        // Inseriamo parametri che faranno fallire il FieldValidator
        when(request.getParameter("username")).thenReturn("a"); // Troppo corto o invalido
        when(request.getParameter("password")).thenReturn("weak"); // Invalido
        when(request.getParameter("biography")).thenReturn("Tentativo hacker");

        when(request.getPart("icon")).thenReturn(null); // Nessuna immagine

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        
        // 1. Il blocco if() della validazione dovrebbe aver bloccato tutto:
        // non ci deve essere nessun setAttribute e nessun reindirizzamento.
        verify(session, never()).setAttribute(anyString(), any());
        verify(response, never()).sendRedirect(anyString());

        // 2. Verifica Database: I dati originali devono essere intatti
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT username FROM Utente_Registrato WHERE email = ?")) {
            
            ps.setString(1, TARGET_EMAIL);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String dbUsername = rs.getString("username");
            
            assertEquals("VecchioUsername", dbUsername, "L'username nel DB NON deve essere cambiato se la validazione fallisce");
        }
    }
}