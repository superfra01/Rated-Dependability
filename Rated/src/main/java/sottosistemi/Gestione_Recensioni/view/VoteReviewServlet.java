package sottosistemi.Gestione_Recensioni.view;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

@WebServlet("/VoteReview")
public class VoteReviewServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// Campo reso final e inizializzato direttamente per eliminare lo smell
	// Naming mantenuto identico all'originale per evitare NoSuchFieldException nei test
	private final RecensioniService RecensioniService = new RecensioniService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			final HttpSession session = request.getSession(true);
			final UtenteBean user = (UtenteBean) session.getAttribute("user");

			// Verifica che l'utente sia loggato
			if (user != null) {
				try {
					// Risoluzione dello smell: gestione NumberFormatException per idFilm
					final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
					
					final String email_recensore = request.getParameter("emailRecensore");
					final boolean valutazione = Boolean.parseBoolean(request.getParameter("valutazione"));

					RecensioniService.addValutazione(user.getEmail(), idFilm, email_recensore, valutazione);
					
				} catch (NumberFormatException e) {
					// Gestione dell'errore se l'ID film non è un numero valido
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().write("Errore: L'ID del film deve essere un valore numerico valido.");
				}
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Devi essere autenticato per votare una recensione.");
			}
		} catch (IOException e) {
			// Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la votazione della recensione.");
			}
		}
	}
}