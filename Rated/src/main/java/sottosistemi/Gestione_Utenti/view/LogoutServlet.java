package sottosistemi.Gestione_Utenti.view;

import sottosistemi.Gestione_Utenti.service.AutenticationService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	// Risolto: Campo reso final e inizializzato direttamente per eliminare init()
	private final AutenticationService authService = new AutenticationService();

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			// La logica di logout invalida la sessione tramite il service
			authService.logout(req.getSession());

			// Risoluzione dello smell: gestione dell'eccezione IOException lanciata dal sendRedirect
			resp.sendRedirect(req.getContextPath() + "/");
			
		} catch (IOException e) {
			// Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
			if (!resp.isCommitted()) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la procedura di logout.");
			}
		}
	}
}