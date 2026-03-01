package sottosistemi.Gestione_Utenti.view;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    // Inizializzazione del Logger
    private static final Logger LOGGER = Logger.getLogger(AggiungiWatchlistServlet.class.getName());

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
                try {
                    // Prevenzione dello smell su getWriter()
                    response.getWriter().write("Devi effettuare il login per gestire la watchlist.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di autorizzazione", e);
                }
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
                try {
                    // Prevenzione dello smell su getWriter()
                    response.getWriter().write("ID Film non valido.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di validazione", ioEx);
                }
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
            try {
                // Prevenzione dello smell su getWriter()
                response.getWriter().write(message);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura della risposta di successo", e);
            }

        } catch (Exception e) {
            // Gestione dependability dello smell IOException su sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento della watchlist.");
                } catch (IOException ioEx) {
                    // Sostituzione del catch silenzioso
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Isolamento dell'eccezione lanciata dal sendRedirect
            response.sendRedirect(request.getContextPath() + "/catalogo");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect al catalogo in doGet", e);
        }
    }
}