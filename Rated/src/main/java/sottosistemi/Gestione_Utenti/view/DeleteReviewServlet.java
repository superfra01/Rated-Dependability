package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DeleteReview")
public class DeleteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Naming mantenuto identico per compatibilità con il field injection del test (Reflection)
    private final RecensioniService RecensioniService = new RecensioniService();
    // Inizializzazione del Logger per tracciare le eccezioni
    private static final Logger LOGGER = Logger.getLogger(DeleteReviewServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        String cp = request.getContextPath();
        try {
            // Risoluzione smell su sendRedirect
            response.sendRedirect((cp != null ? cp : "") + "/profile");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect al profilo in doGet", e);
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Recupero Sessione: Usiamo getSession(true) per combaciare con lo stub del test
            final HttpSession session = request.getSession(true); 
            final UtenteBean user = (session != null) ? (UtenteBean) session.getAttribute("user") : null;
            
            // 2. Controllo Autenticazione
            if (user == null) {
                if (!response.isCommitted()) {
                    String cp = request.getContextPath();
                    try {
                        // Risoluzione smell su sendRedirect (Login)
                        response.sendRedirect((cp != null ? cp : "") + "/login.jsp");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect alla login", e);
                    }
                }
                return;
            }

            // 3. Recupero Parametri
            final String idFilmStr = request.getParameter("DeleteFilmID");
            if (idFilmStr == null || idFilmStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int idFilm = Integer.parseInt(idFilmStr);

            // 4. Esecuzione Business Logic (Cancellazione recensione)
            RecensioniService.deleteRecensione(user.getEmail(), idFilm);

            // 5. Redirect finale (Sincronizzato con il "Wanted" del test: /Rated/profile?visitedUser=AuthorDelete)
            if (!response.isCommitted()) {
                String cp = request.getContextPath();
                try {
                    // Risoluzione smell su sendRedirect (Fine operazione)
                    response.sendRedirect((cp != null ? cp : "") + "/profile?visitedUser=" + user.getUsername());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect finale alla pagina del profilo", e);
                }
            }

        } catch (Exception e) {
            // Gestione dependability dello smell IOException su sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(500, "Si è verificato un errore critico imprevisto.");
                } catch (IOException ioEx) {
                    // Sostituzione del catch silenzioso con log esplicito
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }
}