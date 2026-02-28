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
	
	// Risolto: Campo reso final e inizializzato direttamente per eliminare init()
	// Mantengo la "R" maiuscola per non rompere i tuoi test di integrazione
	private final RecensioniService RecensioniService = new RecensioniService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(true);
		final UtenteBean user = (UtenteBean) session.getAttribute("user");
		
		// Buona pratica: aggiunta verifica null per l'utente
		if (user != null && user.getTipoUtente().equals("MODERATORE")) {
			final String userEmail = request.getParameter("ReviewUserEmail");
			final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
			
			RecensioniService.deleteReports(userEmail, idFilm);

			response.sendRedirect(request.getContextPath() + "/moderator");
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
		}
	}
}