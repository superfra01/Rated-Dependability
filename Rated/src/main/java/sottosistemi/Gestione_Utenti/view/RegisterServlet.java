package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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

@WebServlet("/register")
@MultipartConfig(maxFileSize = 16177215)
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1879879L;
    
    // Risolto: Campi resi final e inizializzati direttamente per eliminare init()
    private final AutenticationService authService = new AutenticationService();
    private final ProfileService profService = new ProfileService();
    private final CatalogoService catalogoService = new CatalogoService();
    // Inizializzazione del Logger per tracciare in sicurezza le eccezioni
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            final HttpSession session = req.getSession(true);
            
            // Variabile locale final
            final List<String> generi = catalogoService.getAllGeneri();
            session.setAttribute("genres", generi);

            // Risoluzione dello smell: gestione delle eccezioni ServletException e IOException lanciate dal forward
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
            
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della pagina di registrazione", e);
            // Gestione dell'errore: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!resp.isCommitted()) {
                try {
                    // Protezione del sendError
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante il caricamento della pagina di registrazione.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final String username = request.getParameter("username");
            final String email = request.getParameter("email");
            final String password = request.getParameter("password");
            final String confirmPassword = request.getParameter("confirm_password");
            final String biography = request.getParameter("biography");
            final String[] generi = request.getParameterValues("genres");
            
            byte[] icon = null; 
            
            final Part filePart = request.getPart("profile_icon");
            if (filePart != null && filePart.getSize() > 0) {
                 try (final InputStream inputStream = filePart.getInputStream()) {
                     icon = inputStream.readAllBytes();
                 }
            }

            if (FieldValidator.validateUsername(username) &&
                FieldValidator.validateEmail(email) &&
                FieldValidator.validatePassword(password) &&
                password.equals(confirmPassword)) {

                final UtenteBean utente = authService.register(username, email, password, biography, icon);
                
                if (generi != null) {
                    for (final String genere : generi) { 
                        profService.addPreferenza(email, genere);
                    }
                }

                if (utente != null) {
                    try {
                        // Risoluzione dello smell: gestione IOException per sendRedirect
                        response.sendRedirect(request.getContextPath() + "/login");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect alla login post-registrazione", e);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try {
                        // Risoluzione dello smell: gestione IOException per getWriter
                        response.getWriter().write("Registration failed. User may already exist.");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di registrazione fallita", e);
                    }
                }

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try {
                    // Protezione getWriter per validazione fallita
                    response.getWriter().write("Invalid form data. Check your inputs.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di validazione form", e);
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante la procedura di registrazione", e);
            // Gestione dell'errore di sistema
            if (!response.isCommitted()) {
                try {
                    // Protezione del sendError
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la procedura di registrazione.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500 in doPost, stream disconnesso", ioEx);
                }
            }
        }
    }
}