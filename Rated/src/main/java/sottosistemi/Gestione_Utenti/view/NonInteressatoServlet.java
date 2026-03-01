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

@WebServlet("/NonInteressatoServlet")
public class NonInteressatoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Campo reso final e inizializzato direttamente
    private final ProfileService profileService = new ProfileService();
    // Inizializzazione del Logger per tracciare in sicurezza le eccezioni
    private static final Logger LOGGER = Logger.getLogger(NonInteressatoServlet.class.getName());

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
                try {
                    // Risoluzione dello smell: gestione IOException per getWriter
                    response.getWriter().write("Devi effettuare il login.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di autorizzazione", e);
                }
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
                try {
                    // Protezione getWriter per Bad Request
                    response.getWriter().write("ID Film non valido.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di validazione ID", ioEx);
                }
                return;
            }

            if (filmId != -1) {
                // Variabili locali final
                final boolean isPresent = profileService.isFilmInWatchlist(utenteSessione.getEmail(), filmId);
                profileService.ignoreFilm(utenteSessione.getEmail(), filmId);
                
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try {
                    // Protezione getWriter per film non identificato
                    response.getWriter().write("Impossibile identificare il film.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore film mancante", e);
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante l'elaborazione in doPost", e);
            // Gestione dell'errore di sistema: invio di un codice di errore 500 protetto
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante l'elaborazione della richiesta.");
                } catch (IOException ioEx) {
                    // Risoluzione smell per sendError all'interno del catch
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Risoluzione dello smell: gestione IOException per sendRedirect
            response.sendRedirect(request.getContextPath() + "/index.jsp"); 
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il redirect in doGet", e);
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il reindirizzamento.");
                } catch (IOException ioEx) {
                    // Risoluzione smell per sendError all'interno del catch
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500 in doGet, stream disconnesso", ioEx);
                }
            }
        }
    }
}