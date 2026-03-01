package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/profileModify")
@MultipartConfig(maxFileSize = 16177215)
public class ProfileModifyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	// Risolto: Campo reso final e inizializzato direttamente per eliminare init()
	// Naming mantenuto identico per non rompere i test di integrazione
	private final ProfileService ProfileService = new ProfileService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			final String username = request.getParameter("username");
			final String email = request.getParameter("email");
			final String password = request.getParameter("password");
			final String biography = request.getParameter("biography");
			
			byte[] icon = null; 

			// Risoluzione dello smell: gestione delle eccezioni IOException e ServletException lanciate da getPart
			final Part filePart = request.getPart("icon");
			
			if (filePart != null && filePart.getSize() > 0) {
				try (final InputStream inputStream = filePart.getInputStream()) {
					icon = inputStream.readAllBytes();
				}
			}

			// Validazione e aggiornamento
			if (FieldValidator.validateUsername(username) && FieldValidator.validatePassword(password)) {
				
				final UtenteBean utente = ProfileService.ProfileUpdate(username, email, password, biography, icon);
				final HttpSession session = request.getSession(true);
				
				// Pulizia: gestiamo l'eventuale utente null in modo sicuro
				if (utente != null) {
					session.setAttribute("user", utente);
					session.setAttribute("visitedUser", utente);
					response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + utente.getUsername());
				} else {
					// Se l'update fallisce, gestiamo l'errore con un redirect alla home
					response.sendRedirect(request.getContextPath() + "/");
				}
			} else {
				// Gestione errore validazione (opzionale: redirect con messaggio d'errore)
				response.sendRedirect(request.getContextPath() + "/profile?error=invalidInput");
			}
			
		} catch (IOException | ServletException e) {
			// Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la modifica del profilo.");
			}
		}
	}
}