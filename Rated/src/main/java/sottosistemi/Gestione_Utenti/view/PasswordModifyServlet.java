package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/passwordModify")
public class PasswordModifyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Risolto: Campo reso final e inizializzato direttamente per rimuovere init()
	// Naming mantenuto identico per non rompere i test di integrazione
	private final ProfileService ProfileService = new ProfileService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String email = request.getParameter("email");
		final String password = request.getParameter("password");
		
		// Validazione della password prima dell'aggiornamento
		if (FieldValidator.validatePassword(password)) {
			// Risolto: variabili locali final
			final UtenteBean utente = ProfileService.PasswordUpdate(email, password);
			
			final HttpSession session = request.getSession(true);
			session.setAttribute("user", utente);
			
			// Pulizia: usiamo direttamente l'oggetto 'utente' appena aggiornato
			if (utente != null) {
				response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + utente.getUsername());
			} else {
				// Fallback in caso di errore nell'aggiornamento
				response.sendRedirect(request.getContextPath() + "/");
			}
		} else {
			// Gestione errore validazione (opzionale: potresti aggiungere un messaggio d'errore)
			response.sendRedirect(request.getContextPath() + "/profile?error=invalidPassword");
		}
	}
}