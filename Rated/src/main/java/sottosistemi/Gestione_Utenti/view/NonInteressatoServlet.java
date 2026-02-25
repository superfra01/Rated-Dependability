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

@WebServlet("/NonInteressatoServlet")
public class NonInteressatoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // VARIABILE DI ISTANZA
    private ProfileService profileService;

    public NonInteressatoServlet() {
        super();
    }

    @Override
    public void init() {
        // Inizializzazione spostata qui
        profileService = new ProfileService();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

        if (utenteSessione == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Devi effettuare il login.");
            return;
        }

        String filmIdStr = request.getParameter("filmId");
        int filmId = -1;
        
        try {
            if (filmIdStr != null && !filmIdStr.isEmpty()) {
                filmId = Integer.parseInt(filmIdStr);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ID Film non valido.");
            return;
        }

        if (filmId != -1) {
            // FIX: Usiamo la variabile di istanza
            boolean isPresent = profileService.isFilmInWatchlist(utenteSessione.getEmail(), filmId);
            profileService.ignoreFilm(utenteSessione.getEmail(), filmId);
            
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Impossibile identificare il film.");
        }
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("/WEB-INF/jsp/HomePage.jsp");
    }
}