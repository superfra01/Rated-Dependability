package sottosistemi.Gestione_Recensioni.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ModerationService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/reportedReviewAndWarn")
public class RimuoviReviewAndWarnServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Risolto: Campi resi final e inizializzati immediatamente per eliminare init()
	// Naming mantenuto identico all'originale per compatibilità con i test
	private final RecensioniService RecensioniService = new RecensioniService();
	private final ModerationService ModerationService = new ModerationService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(true);
		final UtenteBean user = (UtenteBean) session.getAttribute("user");
		
		// Buona pratica: controllo null sull'utente
		if (user != null && "MODERATORE".equals(user.getTipoUtente())) {
			final String userEmail = request.getParameter("ReviewUserEmail");
			final int idFilm = Integer.parseInt(request.getParameter("idFilm"));

			RecensioniService.deleteRecensione(userEmail, idFilm);
			ModerationService.warn(userEmail);

			response.sendRedirect(request.getContextPath() + "/moderator");
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
		}
	}
}