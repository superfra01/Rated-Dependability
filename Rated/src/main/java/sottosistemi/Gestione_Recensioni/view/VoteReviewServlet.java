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
	private RecensioniService RecensioniService;

	@Override
	public void init() {
		RecensioniService = new RecensioniService();
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(true);
		final UtenteBean user = (UtenteBean) session.getAttribute("user");
		final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
		final String email_recensore = request.getParameter("emailRecensore");
		final boolean valutazione = Boolean.parseBoolean(request.getParameter("valutazione"));

		RecensioniService.addValutazione(user.getEmail(), idFilm, email_recensore, valutazione);
	}
}