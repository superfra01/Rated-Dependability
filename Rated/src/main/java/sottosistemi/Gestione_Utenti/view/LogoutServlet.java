package sottosistemi.Gestione_Utenti.view;

import sottosistemi.Gestione_Utenti.service.AutenticationService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Campo final e inizializzato direttamente per garantire l'immutabilità
    private final AutenticationService authService = new AutenticationService();

    @Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			authService.logout(req.getSession());
			resp.sendRedirect(req.getContextPath() + "/");
		} catch (Exception e) {
			if (!resp.isCommitted()) resp.sendError(500, "Si è verificato un errore durante la procedura di logout.");
		}
	}

    /**
     * Il metodo POST delega al GET per supportare logout da form o bottoni.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
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
            // Il client ha chiuso la connessione prematuramente o buffer compromesso
            // In un logout, il lavoro del server (invalidare sessione) è comunque fatto.
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
                // Stream compromesso, il thread termina in modo pulito senza lanciare altre eccezioni.
            }
        }
    }
}