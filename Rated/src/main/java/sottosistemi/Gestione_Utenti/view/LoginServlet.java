package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import utilities.FieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// Campo reso final e inizializzato direttamente
	private final AutenticationService authService = new AutenticationService();

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			// Risoluzione dello smell: gestione delle eccezioni ServletException e IOException lanciate dal forward
			req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
		} catch (ServletException | IOException e) {
			if (!resp.isCommitted()) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento della pagina di login.");
			}
		}
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			final String email = request.getParameter("email");
			final String password = request.getParameter("password");
			final UtenteBean utente = authService.login(email, password);

			// Validazione dei campi
			final boolean isEmailValid = FieldValidator.validateEmail(email);
			final boolean isPasswordValid = FieldValidator.validatePassword(password);

			if (isEmailValid && isPasswordValid) {
				if (utente != null) {
					final HttpSession session = request.getSession(true);
					session.setAttribute("user", utente);
					// Risoluzione dello smell: gestione IOException per sendRedirect
					response.sendRedirect(request.getContextPath() + "/");
				} else {
					// Imposta un attributo di errore e inoltra la richiesta alla JSP
					request.setAttribute("loginError", "Email o password non valide.");
					request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
				}
			} else {
				// Imposta un attributo di errore per input non validi
				final String errorMessage = "Errore di LogIn";
				request.setAttribute("loginError", errorMessage);
				request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
			}
		} catch (ServletException | IOException e) {
			// Gestione dell'errore di sistema
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante l'autenticazione.");
			}
		}
	}
}