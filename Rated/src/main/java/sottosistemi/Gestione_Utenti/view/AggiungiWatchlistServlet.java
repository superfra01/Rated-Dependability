package sottosistemi.Gestione_Utenti.view;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;

@WebServlet("/AggiungiWatchlistServlet")
public class AggiungiWatchlistServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ProfileService profileService;

    public AggiungiWatchlistServlet() {
        super();
    }

    @Override
    public void init() {
        profileService = new ProfileService();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

        // Controllo Login
        if (utenteSessione == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("Devi effettuare il login per gestire la watchlist.");
            return;
        }

        // Recupero parametri
        String filmIdStr = request.getParameter("filmId");
        int filmId = -1;
        
        try {
            if (filmIdStr != null && !filmIdStr.isEmpty()) {
                filmId = Integer.parseInt(filmIdStr);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            response.getWriter().write("ID Film non valido.");
            return;
        }

        if (filmId != -1) {
                // Logica di toggle (aggiungi/rimuovi)
                boolean isPresent = profileService.isFilmInWatchlist(utenteSessione.getEmail(), filmId);
                
                if (isPresent) {
                    profileService.rimuoviDallaWatchlist(utenteSessione.getEmail(), filmId);
                    response.getWriter().write("Film rimosso dalla watchlist.");
                } else {
                    profileService.aggiungiAllaWatchlist(utenteSessione.getEmail(), filmId);
                    response.getWriter().write("Film aggiunto alla watchlist.");
                }
                
                response.setStatus(HttpServletResponse.SC_OK); // 200
                
            
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            response.getWriter().write("Impossibile identificare il film.");
        }
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("catalogo.jsp");
    }
}