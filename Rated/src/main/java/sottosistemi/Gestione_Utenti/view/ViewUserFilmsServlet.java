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
    
    // VARIABILI DI ISTANZA
    private ProfileService profileService;
    private CatalogoService catalogoService;

    public ViewUserFilmsServlet() {
        super();
    }

    @Override
    public void init() {
        // Inizializzazione spostata qui
        profileService = new ProfileService();
        catalogoService = new CatalogoService();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        HttpSession session = request.getSession();

        try {
            // 1. Recupero dati Utente visitato tramite ProfileService
            UtenteBean visitedUser = profileService.findByUsername(username); 
            
            if (visitedUser == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Utente non trovato");
                return;
            }
            session.setAttribute("visitedUser", visitedUser);

            // 2. Recupero liste film tramite ProfileService (usando la variabile di istanza)
            List<FilmBean> watchedList = profileService.retrieveWatchedFilms(username); 
            List<FilmBean> watchlist = profileService.retrieveWatchlist(username);

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

        } catch (Exception e) { 
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati utente.");
        }
    }

    private void populateGenres(HttpSession session, List<FilmBean> films, CatalogoService service) throws SQLException {
        for (FilmBean film : films) {
            String key = film.getIdFilm() + "Generi";
            if (session.getAttribute(key) == null) {
                List<FilmGenereBean> generi = service.getGeneri(film.getIdFilm());
                session.setAttribute(key, generi);
            }
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}