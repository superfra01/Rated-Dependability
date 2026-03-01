package sottosistemi.Gestione_Utenti.view;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.UtenteBean;

@WebServlet("/userFilms")
public class ViewUserFilmsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Naming mantenuto per compatibilità con l'iniezione tramite Reflection nei test
    private final ProfileService profileService = new ProfileService();
    private final CatalogoService catalogoService = new CatalogoService();

    public ViewUserFilmsServlet() {
        super();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String username = request.getParameter("username");
        final HttpSession session = request.getSession();

        try {
            // 1. Recupero dati Utente (Sincronizzato con il Service)
            final UtenteBean visitedUser = profileService.findByUsername(username); 
            
            if (visitedUser == null) {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Utente non trovato");
                }
                return;
            }
            
            session.setAttribute("visitedUser", visitedUser);

            // 2. Recupero liste film (Null-safe)
            // RIPRISTINO: Usiamo l'username invece dell'email. Molti Service per le query "Visto"
            // utilizzano l'username se l'URL passava originariamente quello come identificatore pubblico.
            final List<FilmBean> watchedListRaw = profileService.retrieveWatchedFilms(username);
            final List<FilmBean> watchlistRaw = profileService.retrieveWatchlist(username);
            
            final List<FilmBean> watchedList = (watchedListRaw != null) ? watchedListRaw : new ArrayList<>();
            final List<FilmBean> watchlist = (watchlistRaw != null) ? watchlistRaw : new ArrayList<>();

            session.setAttribute("watchedList", watchedList);
            session.setAttribute("watchlist", watchlist);

            // 3. Recupero Generi per ogni film nelle liste
            populateGenres(session, watchedList, catalogoService);
            populateGenres(session, watchlist, catalogoService);

            // 4. Forward alla JSP
            final RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/userFilms.jsp");
            dispatcher.forward(request, response);

        } catch (final Exception e) { 
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante il recupero dei dati dell'utente.");
                } catch (IOException ioEx) {
                    // Silenzioso
                }
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private void populateGenres(final HttpSession session, final List<FilmBean> films, final CatalogoService service) throws SQLException {
        for (final FilmBean film : films) {
            if (film != null) {
                final String key = film.getIdFilm() + "Generi";
                if (session.getAttribute(key) == null) {
                    final List<FilmGenereBean> generi = service.getGeneri(film.getIdFilm());
                    session.setAttribute(key, (generi != null) ? generi : new ArrayList<>());
                }
            }
        }
    }
}