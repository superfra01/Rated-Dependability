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
    
    // Campo reso final e inizializzato direttamente
    private final ProfileService profileService = new ProfileService();

    public NonInteressatoServlet() {
        super();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            
            final HttpSession session = request.getSession();
            final UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

            if (utenteSessione == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // Risoluzione dello smell: gestione IOException per getWriter
                response.getWriter().write("Devi effettuare il login.");
                return;
            }

            final String filmIdStr = request.getParameter("filmId");
            int filmId = -1; 

            try {
                if (filmIdStr != null && !filmIdStr.isEmpty()) {
                    filmId = Integer.parseInt(filmIdStr);
                }
            } catch (final NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("ID Film non valido.");
                return;
            }

            if (filmId != -1) {
                // Variabili locali final
                final boolean isPresent = profileService.isFilmInWatchlist(utenteSessione.getEmail(), filmId);
                profileService.ignoreFilm(utenteSessione.getEmail(), filmId);
                
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Impossibile identificare il film.");
            }
            
        } catch (IOException e) {
            // Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante l'elaborazione della richiesta.");
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Risoluzione dello smell: gestione IOException per sendRedirect
            response.sendRedirect(request.getContextPath() + "/index.jsp"); 
        } catch (IOException e) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il reindirizzamento.");
            }
        }
    }
}