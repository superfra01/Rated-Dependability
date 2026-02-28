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
	
	// Risolto: Campo reso final e inizializzato direttamente per eliminare lo smell
	// Naming mantenuto identico all'originale per evitare NoSuchFieldException nei test
	private final RecensioniService RecensioniService = new RecensioniService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(true);
		final UtenteBean user = (UtenteBean) session.getAttribute("user");
		
		// Buona pratica: verifica che l'utente sia loggato
		if (user != null) {
			final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
			final String email_recensore = request.getParameter("emailRecensore");
			final boolean valutazione = Boolean.parseBoolean(request.getParameter("valutazione"));

			RecensioniService.addValutazione(user.getEmail(), idFilm, email_recensore, valutazione);
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}