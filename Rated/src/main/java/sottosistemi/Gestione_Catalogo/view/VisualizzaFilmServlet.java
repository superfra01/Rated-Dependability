package sottosistemi.Gestione_Catalogo.view;

import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import model.Entity.ValutazioneBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/film")
public class VisualizzaFilmServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final CatalogoService catalogoService = new CatalogoService();
    private final RecensioniService recensioniService = new RecensioniService();
    private final ProfileService profileService = new ProfileService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession(true);

            // 1. Validazione ID Film
            final String idFilmStr = request.getParameter("idFilm");
            if (idFilmStr == null || idFilmStr.isEmpty()) {
                response.sendRedirect("catalogo.jsp");
                return;
            }

            final int idFilm;
            try {
                idFilm = Integer.parseInt(idFilmStr);
            } catch (final NumberFormatException e) {
                response.sendRedirect("catalogo.jsp");
                return;
            }

            // 2. Recupero Dati Principali (Critici)
            final FilmBean film = catalogoService.getFilm(idFilm);
            if (film == null) {
                response.sendRedirect("catalogo.jsp");
                return;
            }
            session.setAttribute("film", film);

            // 3. Recupero Dati Correlati (Non critici: se falliscono, la pagina deve comunque caricare)
            try {
                final List<FilmGenereBean> generi = catalogoService.getGeneri(idFilm);
                session.setAttribute("Generi", (generi != null) ? generi : new ArrayList<>());

                final List<RecensioneBean> recensioni = recensioniService.GetRecensioni(idFilm);
                session.setAttribute("recensioni", (recensioni != null) ? recensioni : new ArrayList<>());

                if (recensioni != null && !recensioni.isEmpty()) {
                    final HashMap<String, String> utenti = profileService.getUsers(recensioni);
                    session.setAttribute("users", utenti);
                } else {
                    session.removeAttribute("users");
                }
            } catch (Exception e) {
                // Silenziamo l'errore per i dati non essenziali o logghiamolo internamente
                session.setAttribute("recensioni", new ArrayList<>());
            }

            // 4. Gestione Stato Utente (Watchlist, Visti, Valutazioni)
            handleUserContext(session, idFilm);

            // 5. Inoltro alla JSP
            try {
                request.getRequestDispatcher("/WEB-INF/jsp/film.jsp").forward(request, response);
            } catch (ServletException | IOException e) {
                handleCriticalError(response, "Errore nel caricamento della vista film.");
            }

        } catch (Exception e) {
            handleCriticalError(response, "Si è verificato un errore imprevisto.");
        }
    }

    private void handleUserContext(HttpSession session, int idFilm) {
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        boolean isWatched = false;
        boolean inWatchlist = false;

        if (user != null) {
            try {
                final String email = user.getEmail();
                final String username = user.getUsername();

                // Valutazioni
                final HashMap<String, ValutazioneBean> valutazioni = recensioniService.GetValutazioni(idFilm, email);
                session.setAttribute("valutazioni", valutazioni);

                // Controllo Visti
                final List<FilmBean> watchedList = profileService.retrieveWatchedFilms(username);
                if (watchedList != null) {
                    isWatched = watchedList.stream().anyMatch(f -> f.getIdFilm() == idFilm);
                }

                // Controllo Watchlist
                final List<FilmBean> watchlist = profileService.retrieveWatchlist(username);
                if (watchlist != null) {
                    inWatchlist = watchlist.stream().anyMatch(f -> f.getIdFilm() == idFilm);
                }
            } catch (Exception e) {
                // Se fallisce il contesto utente, settiamo default per non bloccare la pagina
            }
        }
        session.setAttribute("watched", isWatched);
        session.setAttribute("inwatchlist", inWatchlist);
    }

    private void redirectSafe(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + path);
        }
    }

    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException e) {
                // Stream chiuso, nulla da fare
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}