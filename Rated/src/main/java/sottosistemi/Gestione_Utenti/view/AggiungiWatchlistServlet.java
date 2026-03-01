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

    private final ProfileService profileService = new ProfileService();

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // 1. Configurazione della risposta
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            // FIX: getSession() standard per compatibilità con il mock del test
            final HttpSession session = request.getSession(); 
            final UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

            // 2. Controllo Autenticazione (Messaggio esatto per il test Unauthorized)
            if (utenteSessione == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Devi effettuare il login per gestire la watchlist.");
                return;
            }

            // 3. Recupero e Validazione parametri
            final String filmIdStr = request.getParameter("filmId");
            int filmId;
            try {
                if (filmIdStr == null || filmIdStr.isEmpty()) {
                    throw new NumberFormatException();
                }
                filmId = Integer.parseInt(filmIdStr);
            } catch (final NumberFormatException e) {
                // FIX: Status 400 E scrittura messaggio esatto per il test BadRequest
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("ID Film non valido.");
                return;
            }

            // 4. Esecuzione Business Logic
            final String email = utenteSessione.getEmail();
            final boolean isPresent = profileService.isFilmInWatchlist(email, filmId);
            
            String message;
            if (isPresent) {
                profileService.rimuoviDallaWatchlist(email, filmId);
                message = "Film rimosso dalla watchlist.";
            } else {
                profileService.aggiungiAllaWatchlist(email, filmId);
                message = "Film aggiunto alla watchlist.";
            }

            // 5. Scrittura risposta di successo (Status 200 OK)
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(message);

        } catch (Exception e) {
            // Gestione dependability dello smell IOException su sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento della watchlist.");
                } catch (IOException ioEx) {
                    // Silenzioso
                }
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/catalogo");
    }
}