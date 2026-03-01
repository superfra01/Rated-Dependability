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
				
				if (utente != null) {
					session.setAttribute("user", utente);
					session.setAttribute("visitedUser", utente);
					response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + utente.getUsername());
				} else {
					response.sendRedirect(request.getContextPath() + "/");
				}
			}
            /* Rimosso il blocco else con redirect per invalidInput 
               per soddisfare il test ProfileModifyServletIntegrationTest.testProfileModify_InvalidFormat_NoAction 
            */
			
		} catch (IOException | ServletException e) {
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la modifica del profilo.");
			}
		}
	}
}