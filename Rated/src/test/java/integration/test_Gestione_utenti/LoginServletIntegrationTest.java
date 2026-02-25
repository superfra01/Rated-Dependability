package integration.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import integration.DatabaseSetupForTest;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.view.LoginServlet;

// IMPORTANTE: Assicurati di importare la tua utility per le password
import utilities.PasswordUtility; 

public class LoginServletIntegrationTest {

    private LoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private DataSource dataSource;
    
    private AutenticationService authService;

    // Definiamo delle credenziali valide che passino sicuramente il FieldValidator
    private static final String TEST_EMAIL = "login.tester@example.com";
    private static final String TEST_PASSWORD = "Password123!"; // Formato valido (Maiuscola, numero, speciale)

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inizializzazione DB in memoria H2
        dataSource = DatabaseSetupForTest.getH2DataSource();
        
        // --- PREPARAZIONE DATI ISOLATI PER IL TEST ---
        try (Connection conn = dataSource.getConnection()) {
            
            // Generiamo l'hash della password usando la tua utility reale.
            // NOTA: Se il metodo nella tua classe PasswordUtility si chiama diversamente 
            // (es. hashPassword, encrypt, ecc.), modificalo qui sotto.
            String hashedPassword = PasswordUtility.hashPassword(TEST_PASSWORD);

            // Inseriamo l'utente nel database H2
            String userSql = "MERGE INTO Utente_Registrato (email, username, Password, Tipo_Utente) KEY(email) VALUES (?, 'LoginTester', ?, 'RECENSORE')";
            try(PreparedStatement ps = conn.prepareStatement(userSql)) { 
                ps.setString(1, TEST_EMAIL);
                ps.setString(2, hashedPassword);
                ps.executeUpdate(); 
            }
        }

        // 2. Inizializzazione Service
        authService = new AutenticationService();

        // 3. Inizializzazione Servlet
        servlet = new LoginServlet();
        servlet.init();
        
        // 4. Iniezione del Service tramite Reflection (Nome variabile "authService")
        Field authField = LoginServlet.class.getDeclaredField("authService");
        authField.setAccessible(true);
        authField.set(servlet, authService);

        // 5. Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        // Setup base per i dispatch
        when(request.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);
    }

    @Test
    public void testDoGet_MostraPaginaLogin() throws Exception {
        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        // Verifica che inoltri semplicemente la richiesta alla JSP del login
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testLogin_Success_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("email")).thenReturn(TEST_EMAIL);
        when(request.getParameter("password")).thenReturn(TEST_PASSWORD);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/Rated");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // 1. Verifichiamo che l'utente sia stato estratto dal DB e salvato in sessione
        ArgumentCaptor<UtenteBean> userCaptor = ArgumentCaptor.forClass(UtenteBean.class);
        verify(session).setAttribute(eq("user"), userCaptor.capture());
        
        UtenteBean utenteLoggato = userCaptor.getValue();
        assertEquals(TEST_EMAIL, utenteLoggato.getEmail(), "L'email dell'utente loggato deve coincidere");

        // 2. Verifichiamo che ci sia stato il redirect alla HomePage ("/")
        verify(response).sendRedirect("/Rated/");
    }

    @Test
    public void testLogin_CredenzialiErrate_Integration() throws Exception {
        // --- ARRANGE ---
        when(request.getParameter("email")).thenReturn(TEST_EMAIL);
        // Inseriamo una password sintatticamente valida ma SBAGLIATA rispetto al DB
        when(request.getParameter("password")).thenReturn("PasswordErrata99!");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // 1. Verifica che venga impostato il messaggio di errore corretto
        verify(request).setAttribute("loginError", "Email o password non valide.");
        
        // 2. Verifica che venga ricaricata la pagina di login (niente redirect)
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testLogin_FormatoInvalido_Integration() throws Exception {
        // --- ARRANGE ---
        // Inseriamo parametri che faranno fallire il FieldValidator
        when(request.getParameter("email")).thenReturn("email_non_valida");
        when(request.getParameter("password")).thenReturn("123");

        // --- ACT ---
        servlet.doPost(request, response);

        // --- ASSERT ---
        // 1. Verifica che il FieldValidator abbia bloccato l'esecuzione impostando l'errore
        verify(request).setAttribute("loginError", "Errore di LogIn");
        
        // 2. Verifica forward alla pagina di login
        verify(dispatcher).forward(request, response);
    }
}
