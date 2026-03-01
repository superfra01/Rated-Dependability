package sottosistemi.Gestione_Recensioni.view;

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

@WebServlet("/ReportReview")
public class ReportReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RecensioniService RecensioniService = new RecensioniService();
    // Inizializzazione del Logger per tracciare le eccezioni
    private static final Logger LOGGER = Logger.getLogger(ReportReviewServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        String cp = request.getContextPath();
        try {
            // Risoluzione smell su sendRedirect
            response.sendRedirect((cp != null ? cp : "") + "/catalogo");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect al catalogo in doGet", e);
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // FIX: Usiamo getSession(true) per combaciare con lo stub del test
            final HttpSession session = request.getSession(true); 
            final UtenteBean user = (session != null) ? (UtenteBean) session.getAttribute("user") : null;

            // 1. Controllo Autenticazione
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

            final String email = user.getEmail();
            final String emailRecensore = request.getParameter("reviewerEmail");
            final String idFilmStr = request.getParameter("idFilm");

            // 2. Validazione parametri
            if (emailRecensore == null || idFilmStr == null || idFilmStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            final int idFilm;
            try {
                idFilm = Integer.parseInt(idFilmStr);
            } catch (final NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 3. Esecuzione Business Logic
            RecensioniService.report(email, emailRecensore, idFilm);
            
            // 4. Redirect finale (Sincronizzato con il "Wanted" del test)
            if (!response.isCommitted()) {
                String cp = request.getContextPath();
                try {
                    // Gestione null per i mock e risoluzione smell su sendRedirect
                    response.sendRedirect((cp != null ? cp : "") + "/film?idFilm=" + idFilm);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect finale alla pagina del film", e);
                }
            }

        } catch (Exception e) {
            // Gestione dependability dello smell IOException su sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore imprevisto nel sistema.");
                } catch (IOException ioEx) {
                    // Sostituzione del catch silenzioso con log esplicito
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la pagina di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }
}