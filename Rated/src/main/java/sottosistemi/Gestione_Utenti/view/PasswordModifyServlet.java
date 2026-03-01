package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/passwordModify")
public class PasswordModifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Risolto: Campo reso final e inizializzato direttamente per rimuovere init()
    // Naming mantenuto identico per non rompere i test di integrazione
    private final ProfileService ProfileService = new ProfileService();
    // Inizializzazione del Logger per tracciare le eccezioni in modo affidabile
    private static final Logger LOGGER = Logger.getLogger(PasswordModifyServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final String email = request.getParameter("email");
            final String password = request.getParameter("password");
            
            // Validazione della password prima dell'aggiornamento
            if (FieldValidator.validatePassword(password)) {
                // Risolto: variabili locali final
                final UtenteBean utente = ProfileService.PasswordUpdate(email, password);

                final HttpSession session = request.getSession(true);
                session.setAttribute("user", utente);

                // Pulizia: usiamo direttamente l'oggetto 'utente' appena aggiornato
                if (utente != null) {
                    try {
                        // Risoluzione dello smell: gestione isolata IOException per sendRedirect
                        response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + utente.getUsername());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect al profilo aggiornato", e);
                    }
                } else {
                    try {
                        // Fallback in caso di errore nell'aggiornamento
                        response.sendRedirect(request.getContextPath() + "/");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect di fallback", e);
                    }
                }
            } else {
                try {
                    // Gestione errore validazione
                    response.sendRedirect(request.getContextPath() + "/profile?error=invalidPassword");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect per password non valida", e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante l'aggiornamento della password", e);
            // Gestione dell'errore di sistema: invio di un codice di errore 500 protetto
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante l'aggiornamento della password.");
                } catch (IOException ioEx) {
                    // Risoluzione dello smell su sendError
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }
}