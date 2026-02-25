package integration.test_Gestione_utenti;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.view.LogoutServlet;

public class LogoutServletIntegrationTest {

    private LogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    
    // CAMBIO FONDAMENTALE: Usiamo un Mock invece di istanziare la classe reale!
    private AutenticationService authServiceMock;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Creiamo un finto AutenticationService con Mockito
        // In questo modo evitiamo qualsiasi chiamata al costruttore reale che fa crashare JNDI/Tomcat
        authServiceMock = mock(AutenticationService.class);

        // 2. Inizializzazione della Servlet
        servlet = new LogoutServlet();
        
        // Chiamiamo init() (questo istanzierà il service reale per un millisecondo)
        // Se il tuo metodo init() di LogoutServlet lancia l'eccezione, puoi rimuovere servlet.init() 
        // perché noi sovrascriveremo comunque la variabile nel passaggio successivo.
        try {
            servlet.init();
        } catch (Exception e) {
            // Ignoriamo eventuali eccezioni JNDI lanciate dal costruttore originale in init()
        }
        
        // 3. Iniezione del Service MOCK tramite Reflection
        Field authField = LogoutServlet.class.getDeclaredField("authService");
        authField.setAccessible(true);
        // Iniettiamo il nostro MOCK che è sicuro e non chiama il DB
        authField.set(servlet, authServiceMock);

        // 4. Inizializzazione dei Mock HTTP
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/Rated");
    }

    @Test
    public void testLogout_Success_Integration() throws Exception {
        // --- ACT ---
        servlet.doGet(request, response);

        // --- ASSERT ---
        
        // 1. Verifichiamo che la Servlet abbia invocato il metodo logout del Service
        verify(authServiceMock).logout(session);

        // 2. Verifica HTTP: Assicuriamoci che l'utente venga reindirizzato alla Home Page "/"
        verify(response).sendRedirect("/Rated/");
    }
}