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

@WebServlet("/ModificaPreferenzeServlet")
public class ModificaPreferenzeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Campo reso final e inizializzato direttamente per eliminare init()
    private final ProfileService profileService = new ProfileService();
    // Inizializzazione del Logger per tracciare le eccezioni
    private static final Logger LOGGER = Logger.getLogger(ModificaPreferenzeServlet.class.getName());

    public ModificaPreferenzeServlet() {
        super();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession();
            final UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

            // 1. Controllo Autenticazione: L'utente è loggato?
            if (utenteSessione == null) {
                try {
                    // Risoluzione dello smell: gestione IOException per sendRedirect
                    response.sendRedirect("login.jsp");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect alla login", e);
                }
                return;
            }

            final String targetEmail = request.getParameter("email"); 
            final String[] generiSelezionati = request.getParameterValues("selectedGenres");

            // 2. Controllo Autorizzazione: Chi fa la richiesta è il proprietario dell'account?
            if (targetEmail != null && !targetEmail.equals(utenteSessione.getEmail())) {
                try {
                    // Risoluzione dello smell per sendError
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare le preferenze di questo utente.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Impossibile inviare errore 403, stream disconnesso", e);
                }
                return;
            }

            final String email = utenteSessione.getEmail();
            // 3. Chiama il Service
            profileService.aggiornaPreferenzeUtente(email, generiSelezionati);

            // Aggiorna messaggio di successo in sessione
            session.setAttribute("messaggioSuccesso", "Preferenze aggiornate con successo!");

            // 4. Redirect al profilo
            try {
                // Risoluzione dello smell per sendRedirect
                response.sendRedirect("profile?visitedUser=" + utenteSessione.getUsername());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect al profilo", e);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante la modifica delle preferenze", e);
            // Gestione dell'errore di sistema: invio di un codice di errore 500 protetto
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la modifica delle preferenze.");
                } catch (IOException ioEx) {
                    // Risoluzione dello smell per sendError
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Risoluzione dello smell: gestione IOException per sendRedirect
            response.sendRedirect("profile.jsp");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il redirect in doGet", e);
            if (!response.isCommitted()) {
                try {
                    // Risoluzione dello smell per sendError
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il reindirizzamento.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500 in doGet, stream disconnesso", ioEx);
                }
            }
        }
    }
}