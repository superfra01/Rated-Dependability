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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/film")
public class VisualizzaFilmServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Service resi final e nomi corretti in camelCase
    private final CatalogoService catalogoService = new CatalogoService();
    private final RecensioniService recensioniService = new RecensioniService();
    private final ProfileService profileService = new ProfileService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession(true);

            // 1. Gestione sicura del parametro idFilm
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
            
            // 2. Recupero Film
            final FilmBean film = catalogoService.getFilm(idFilm);
            if (film == null) {
                response.sendRedirect("catalogo.jsp");
                return;
            }
            session.setAttribute("film", film);
            
            // 3. Recupero Generi
            final List<FilmGenereBean> generi = catalogoService.getGeneri(film.getIdFilm());
            session.setAttribute("Generi", generi);
            
            // 4. Recupero Recensioni
            final List<RecensioneBean> recensioni = recensioniService.GetRecensioni(idFilm);
            session.setAttribute("recensioni", recensioni);
            
            if (recensioni != null && !recensioni.isEmpty()) {
                final HashMap<String, String> utenti = profileService.getUsers(recensioni);
                session.setAttribute("users", utenti);
            } else {
                session.removeAttribute("users");
            }
            
            // 5. Gestione Utente Loggato
            final UtenteBean user = (UtenteBean) session.getAttribute("user");
            boolean isWatched = false; 
            boolean inWatchlist = false; 

            if (user != null) {
                final String email = user.getEmail();
                
                // A. Valutazioni
                final HashMap<String, ValutazioneBean> valutazioni = recensioniService.GetValutazioni(idFilm, email);
                session.setAttribute("valutazioni", valutazioni);
                
                // B. Controllo "VISTI"
                final List<FilmBean> watchedList = profileService.retrieveWatchedFilms(user.getUsername());
                if (watchedList != null) {
                    for (final FilmBean f : watchedList) {
                        if (f.getIdFilm() == idFilm) {
                            isWatched = true;
                            break;
                        }
                    }
                }
                
                // C. Controllo "WATCHLIST"
                final List<FilmBean> watchlist = profileService.retrieveWatchlist(user.getUsername());
                if (watchlist != null) {
                    for (final FilmBean f : watchlist) {
                        if (f.getIdFilm() == idFilm) {
                            inWatchlist = true;
                            break;
                        }
                    }
                }
            }
            
            session.setAttribute("watched", isWatched);
            session.setAttribute("inwatchlist", inWatchlist);
            
            // Risoluzione dello smell: gestione delle eccezioni ServletException e IOException lanciate dal forward o sendRedirect
            request.getRequestDispatcher("/WEB-INF/jsp/film.jsp").forward(request, response);

        } catch (ServletException | IOException e) {
            // Gestione dell'errore: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la visualizzazione dei dettagli del film.");
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}