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

// Import dei Service
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.UtenteBean;

@WebServlet("/userFilms")
public class ViewUserFilmsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Risolto: Campi resi final e inizializzati immediatamente
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
            // 1. Recupero dati Utente visitato tramite ProfileService
            final UtenteBean visitedUser = profileService.findByUsername(username); 
            
            if (visitedUser == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Utente non trovato");
                return;
            }
            session.setAttribute("visitedUser", visitedUser);

            // 2. Recupero liste film (uso operatore ternario per mantenere la variabile final)
            final List<FilmBean> watchedListRaw = profileService.retrieveWatchedFilms(username);
            final List<FilmBean> watchlistRaw = profileService.retrieveWatchlist(username);

            final List<FilmBean> watchedList = (watchedListRaw != null) ? watchedListRaw : new ArrayList<>();
            final List<FilmBean> watchlist = (watchlistRaw != null) ? watchlistRaw : new ArrayList<>();

            session.setAttribute("watchedList", watchedList);
            session.setAttribute("watchlist", watchlist);

            // 3. Recupero Generi per i film tramite CatalogoService
            populateGenres(session, watchedList, catalogoService);
            populateGenres(session, watchlist, catalogoService);

            // 4. Forward alla pagina JSP
            final RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/userFilms.jsp");
            dispatcher.forward(request, response);

        } catch (final Exception e) { 
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati utente.");
        }
    }

    // Risolto: Parametri final e variabili interne protette
    private void populateGenres(final HttpSession session, final List<FilmBean> films, final CatalogoService service) throws SQLException {
        for (final FilmBean film : films) {
            final String key = film.getIdFilm() + "Generi";
            if (session.getAttribute(key) == null) {
                final List<FilmGenereBean> generi = service.getGeneri(film.getIdFilm());
                session.setAttribute(key, generi);
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}