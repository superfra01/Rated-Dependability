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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    // Aggiunta del Logger
    private static final Logger LOGGER = Logger.getLogger(VisualizzaFilmServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession(true);

            // 1. Validazione ID Film
            final String idFilmStr = request.getParameter("idFilm");
            if (idFilmStr == null || idFilmStr.isEmpty()) {
                // Sostituzione con il metodo helper protetto
                redirectSafe(response, "catalogo.jsp");
                return;
            }

            final int idFilm;
            try {
                idFilm = Integer.parseInt(idFilmStr);
            } catch (final NumberFormatException e) {
                // Sostituzione con il metodo helper protetto
                redirectSafe(response, "catalogo.jsp");
                return;
            }

            // 2. Recupero Dati Principali (Critici)
            final FilmBean film = catalogoService.getFilm(idFilm);
            if (film == null) {
                // Sostituzione con il metodo helper protetto
                redirectSafe(response, "catalogo.jsp");
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
                // Registriamo l'errore non critico nel log
                LOGGER.log(Level.WARNING, "Impossibile recuperare i dati correlati (generi/recensioni) per il film ID: " + idFilm, e);
                session.setAttribute("recensioni", new ArrayList<>());
            }

            // 4. Gestione Stato Utente (Watchlist, Visti, Valutazioni)
            handleUserContext(session, idFilm);

            // 5. Inoltro alla JSP
            try {
                request.getRequestDispatcher("/WEB-INF/jsp/film.jsp").forward(request, response);
            } catch (ServletException | IOException e) {
                LOGGER.log(Level.SEVERE, "Errore nel forward verso la vista film.jsp", e);
                handleCriticalError(response, "Errore nel caricamento della vista film.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore critico imprevisto nel doGet", e);
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
                // Log dell'errore anziché ignorarlo silenziosamente
                LOGGER.log(Level.WARNING, "Errore durante il recupero del contesto utente per il film ID: " + idFilm, e);
            }
        }
        session.setAttribute("watched", isWatched);
        session.setAttribute("inwatchlist", inWatchlist);
    }

    /**
     * Modificato: Gestisce internamente l'eccezione invece di lanciarla,
     * risolvendo lo smell del sendRedirect.
     */
    private void redirectSafe(HttpServletResponse response, String path) {
        if (!response.isCommitted()) {
            try {
                // Rimosso il context path per mantenere il redirect relativo atteso dal test
                response.sendRedirect(path);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect a " + path, e);
            }
        }
    }

    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException e) {
                // Risolto blocco catch vuoto
                LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", e);
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Protezione della chiamata a doGet
        try {
            doGet(request, response);
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'inoltro della richiesta POST al metodo doGet", e);
            handleCriticalError(response, "Errore interno durante l'elaborazione della richiesta.");
        }
    }
}