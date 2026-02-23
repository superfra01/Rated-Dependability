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

/**
 * Servlet implementation class UserFilmsServlet
 */
@WebServlet("/userFilms")
public class ViewUserFilmsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ViewUserFilmsServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Recupero parametri
        String username = request.getParameter("username");
        HttpSession session = request.getSession();

        // Istanziazione Service
        ProfileService profileService = new ProfileService();
        CatalogoService catalogoService = new CatalogoService();

        try {
            // 1. Recupero dati Utente visitato tramite ProfileService
            // Assumiamo che il metodo si chiami findByUsername o retrieveUser
            UtenteBean visitedUser = profileService.findByUsername(username); 
            
            if (visitedUser == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Utente non trovato");
                return;
            }
            session.setAttribute("visitedUser", visitedUser);

            // 2. Recupero liste film tramite ProfileService
            // I metodi nel service dovrebbero incapsulare la logica di vistoDAO e interesseDAO
            List<FilmBean> watchedList = profileService.retrieveWatchedFilms(username); 
            List<FilmBean> watchlist = profileService.retrieveWatchlist(username);

            // Inizializza le liste se i Service tornano null
            if (watchedList == null) watchedList = new ArrayList<>();
            if (watchlist == null) watchlist = new ArrayList<>();

            session.setAttribute("watchedList", watchedList);
            session.setAttribute("watchlist", watchlist);

            // 3. Recupero Generi per i film tramite CatalogoService
            populateGenres(session, watchedList, catalogoService);
            populateGenres(session, watchlist, catalogoService);

            // 4. Forward alla pagina JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/userFilms.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) { // Catch generico o specifico in base alle eccezioni lanciate dai Service
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati utente.");
        }
    }

    /**
     * Metodo helper per popolare i generi in sessione per una lista di film.
     * Utilizza CatalogoService invece del DAO diretto.
     */
    private void populateGenres(HttpSession session, List<FilmBean> films, CatalogoService service) throws SQLException {
        for (FilmBean film : films) {
            String key = film.getIdFilm() + "Generi";
            // Controllo opzionale: se è già in sessione evitiamo la chiamata al service
            if (session.getAttribute(key) == null) {
                // Assumiamo che il metodo del service per recuperare i generi sia getFilmGeneri o simile
                List<FilmGenereBean> generi = service.getGeneri(film.getIdFilm());
                session.setAttribute(key, generi);
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}