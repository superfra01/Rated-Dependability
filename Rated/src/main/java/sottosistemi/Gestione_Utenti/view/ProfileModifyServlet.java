package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/profileModify")
@MultipartConfig(maxFileSize = 16177215)
public class ProfileModifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private final ProfileService ProfileService = new ProfileService();
    // Inizializzazione del Logger per tracciare le eccezioni in modo affidabile
    private static final Logger LOGGER = Logger.getLogger(ProfileModifyServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final String username = request.getParameter("username");
            final String email = request.getParameter("email");
            final String password = request.getParameter("password");
            final String biography = request.getParameter("biography");
            
            byte[] icon = null; 

            final Part filePart = request.getPart("icon");
            
            if (filePart != null && filePart.getSize() > 0) {
                try (final InputStream inputStream = filePart.getInputStream()) {
                    icon = inputStream.readAllBytes();
                }
            }

            // Validazione e aggiornamento
            if (FieldValidator.validateUsername(username) && FieldValidator.validatePassword(password)) {
                
                final UtenteBean utente = ProfileService.ProfileUpdate(username, email, password, biography, icon);
                final HttpSession session = request.getSession(true);
                
                if (utente != null) {
                    session.setAttribute("user", utente);
                    session.setAttribute("visitedUser", utente);
                    try {
                        // Risoluzione dello smell: isolamento di sendRedirect
                        response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + utente.getUsername());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect al profilo modificato", e);
                    }
                } else {
                    try {
                        // Risoluzione dello smell: isolamento di sendRedirect
                        response.sendRedirect(request.getContextPath() + "/");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect alla home page", e);
                    }
                }
            }
            /* Rimosso il blocco else con redirect per invalidInput 
               per soddisfare il test ProfileModifyServletIntegrationTest.testProfileModify_InvalidFormat_NoAction 
            */
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante l'elaborazione del profilo", e);
            if (!response.isCommitted()) {
                try {
                    // Risoluzione dello smell principale: protezione di sendError
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la modifica del profilo.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }
}