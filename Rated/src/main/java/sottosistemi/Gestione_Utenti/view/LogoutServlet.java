package sottosistemi.Gestione_Utenti.view;

import sottosistemi.Gestione_Utenti.service.AutenticationService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Campo final e inizializzato direttamente per garantire l'immutabilità
    private final AutenticationService authService = new AutenticationService();
    // Aggiunta del Logger per tracciare le eccezioni silenziose
    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            authService.logout(req.getSession());
            // UTILIZZO DELL'HELPER: Sostituito resp.sendRedirect
            handleSafeRedirect(req, resp, "/");
        } catch (Exception e) {
            // UTILIZZO DELL'HELPER: Sostituito resp.sendError
            LOGGER.log(Level.SEVERE, "Errore globale durante la procedura di logout", e);
            handleCriticalError(resp, "Si è verificato un errore durante la procedura di logout.");
        }
    }

    /**
     * Il metodo POST delega al GET per supportare logout da form o bottoni.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Messa in sicurezza della delega a doGet
        try {
            doGet(req, resp);
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'inoltro della richiesta POST al metodo doGet", e);
            handleCriticalError(resp, "Errore interno durante il logout.");
        }
    }

    /**
     * Helper per gestire i redirect in modo affidabile, risolvendo lo smell IOException.
     */
    private void handleSafeRedirect(HttpServletRequest request, HttpServletResponse response, String path) {
        try {
            if (!response.isCommitted()) {
                response.sendRedirect(request.getContextPath() + path);
            }
        } catch (IOException e) {
            // Aggiunto il log. In un logout, il lavoro del server (invalidare sessione) è comunque fatto.
            LOGGER.log(Level.WARNING, "Errore di I/O durante il redirect dopo il logout", e);
        }
    }

    /**
     * Helper per gestire errori critici gestendo l'eccezione IOException di sendError.
     * Risolve lo smell: "Handle the following exception that could be thrown by 'sendError': IOException."
     */
    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ioEx) {
                // Sostituzione del catch silenzioso
                LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
            }
        }
    }
}