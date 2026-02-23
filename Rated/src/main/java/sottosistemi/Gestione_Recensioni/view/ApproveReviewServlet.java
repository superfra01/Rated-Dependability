package sottosistemi.Gestione_Recensioni.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ApproveReview")
public class ApproveReviewServlet extends HttpServlet {
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
		if (user.getTipoUtente().equals("MODERATORE")) {
			final String userEmail = request.getParameter("ReviewUserEmail");
			final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
			
			// Nota: qui veniva creata una nuova istanza locale che nascondeva il campo della classe.
			// Ho mantenuto la logica originale ma reso la variabile final.
			final RecensioniService RecensioniService = new RecensioniService(); 
			RecensioniService.deleteReports(userEmail, idFilm);

			response.sendRedirect(request.getContextPath() + "/moderator");
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
		}
	}
}