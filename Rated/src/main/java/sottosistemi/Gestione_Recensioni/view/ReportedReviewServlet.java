package sottosistemi.Gestione_Recensioni.view;

import model.Entity.UtenteBean;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;

import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/moderator")
public class ReportedReviewServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// Risolto: Campi resi final e inizializzati immediatamente
	// Naming mantenuto identico all'originale per non rompere i test di integrazione
	private final CatalogoService CatalogoService = new CatalogoService();
	private final RecensioniService RecensioniService = new RecensioniService();
	private final ProfileService ProfileService = new ProfileService();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			final HttpSession session = request.getSession(true);
			final UtenteBean user = (UtenteBean) session.getAttribute("user");

			if (user != null && "MODERATORE".equals(user.getTipoUtente())) {

				final List<RecensioneBean> recensioni = RecensioniService.GetAllRecensioniSegnalate();
				session.setAttribute("recensioni", recensioni);

				final HashMap<String, String> utenti = ProfileService.getUsers(recensioni);
				session.setAttribute("users", utenti);

				final HashMap<Integer, FilmBean> FilmMap = CatalogoService.getFilms(recensioni);
				session.setAttribute("films", FilmMap);

				// Risoluzione dello smell: gestione delle eccezioni ServletException e IOException lanciate dal forward
				request.getRequestDispatcher("/WEB-INF/jsp/moderator.jsp").forward(request, response);

			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("You can't access the profile page unless you are an authenticated moderator.");
			}
		} catch (ServletException | IOException e) {
			// Gestione dell'errore: invio di un codice di errore 500 se la risposta non è già stata inviata
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante il caricamento della pagina moderatore.");
			}
		}
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// Metodo vuoto
	}
}