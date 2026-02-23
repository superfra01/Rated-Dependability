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
    private CatalogoService CatalogoService;
    private RecensioniService RecensioniService;
    private ProfileService ProfileService;

    @Override
    public void init() {
        CatalogoService = new CatalogoService();
        RecensioniService = new RecensioniService();
        ProfileService = new ProfileService();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession(true);

        // 1. Gestione sicura del parametro idFilm
        String idFilmStr = request.getParameter("idFilm");
        if (idFilmStr == null || idFilmStr.isEmpty()) {
            response.sendRedirect("catalogo.jsp"); // O altra pagina di fallback
            return;
        }

        final int idFilm;
        try {
            idFilm = Integer.parseInt(idFilmStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("catalogo.jsp");
            return;
        }
        
        // 2. Recupero Film
        final FilmBean film = CatalogoService.getFilm(idFilm);
        if (film == null) {
            response.sendRedirect("catalogo.jsp"); // Film non trovato
            return;
        }
        session.setAttribute("film", film);
        
        // 3. Recupero Generi
        final List<FilmGenereBean> generi = CatalogoService.getGeneri(film.getIdFilm());
        session.setAttribute("Generi", generi);
        
        // 4. Recupero Recensioni
        final List<RecensioneBean> recensioni = RecensioniService.GetRecensioni(idFilm);
        session.setAttribute("recensioni", recensioni);
        
        if(recensioni != null && !recensioni.isEmpty()) {
            final HashMap<String, String> utenti = ProfileService.getUsers(recensioni);
            session.setAttribute("users", utenti);
        } else {
            session.removeAttribute("users"); // Pulizia se non ci sono recensioni
        }
        
        // 5. Gestione Utente Loggato (Valutazioni e Liste)
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        
        // Default: false se l'utente non è loggato
        boolean isWatched = false;
        boolean inWatchlist = false;

        if(user != null) {
            final String email = user.getEmail();
            
            // A. Valutazioni (Like/Dislike)
            final HashMap<String, ValutazioneBean> valutazioni = RecensioniService.GetValutazioni(idFilm, email);
            session.setAttribute("valutazioni", valutazioni);
            
            // B. Controllo "VISTI" (Confronto tramite ID)
            List<FilmBean> watchedList = ProfileService.retrieveWatchedFilms(user.getUsername());
            if (watchedList != null) {
                for (FilmBean f : watchedList) {
                    if (f.getIdFilm() == idFilm) {
                        isWatched = true;
                        break;
                    }
                }
            }
            
            // C. Controllo "WATCHLIST" (Confronto tramite ID)
            List<FilmBean> watchlist = ProfileService.retrieveWatchlist(user.getUsername());
            if (watchlist != null) {
                for (FilmBean f : watchlist) {
                    if (f.getIdFilm() == idFilm) {
                        inWatchlist = true;
                        break;
                    }
                }
            }
        }
        
        // Impostiamo gli attributi in sessione (fondamentale per la JSP)
        session.setAttribute("watched", isWatched);
        session.setAttribute("inwatchlist", inWatchlist);
        
        // 6. Forward alla JSP
        request.getRequestDispatcher("/WEB-INF/jsp/film.jsp").forward(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}