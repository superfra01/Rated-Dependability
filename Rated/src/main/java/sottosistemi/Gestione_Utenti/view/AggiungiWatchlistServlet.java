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

    // Campo reso final e inizializzato direttamente per rimuovere init()
    private final ProfileService profileService = new ProfileService();

    public AggiungiWatchlistServlet() {
        super();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            
            final HttpSession session = request.getSession();
            final UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

            // Controllo Login
            if (utenteSessione == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                // Risoluzione dello smell: gestione IOException per getWriter
                response.getWriter().write("Devi effettuare il login per gestire la watchlist.");
                return;
            }

            // Recupero parametri
            final String filmIdStr = request.getParameter("filmId");
            int filmId = -1; 

            try {
                if (filmIdStr != null && !filmIdStr.isEmpty()) {
                    filmId = Integer.parseInt(filmIdStr);
                }
            } catch (final NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                response.getWriter().write("ID Film non valido.");
                return;
            }

            if (filmId != -1) {
                // Logica di toggle (aggiungi/rimuovi)
                final boolean isPresent = profileService.isFilmInWatchlist(utenteSessione.getEmail(), filmId);
                
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

        } catch (IOException e) {
            // Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la gestione della watchlist.");
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Risoluzione dello smell: gestione IOException per sendRedirect
            response.sendRedirect("catalogo.jsp");
        } catch (IOException e) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il reindirizzamento.");
            }
        }
    }
}