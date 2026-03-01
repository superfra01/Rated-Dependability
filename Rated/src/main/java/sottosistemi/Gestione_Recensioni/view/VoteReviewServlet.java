package sottosistemi.Gestione_Recensioni.view;

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
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

@WebServlet("/VoteReview")
public class VoteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Naming mantenuto identico all'originale per compatibilità con i test (Reflection injection)
    private final RecensioniService RecensioniService = new RecensioniService();
    
    // Inizializzazione del Logger per tracciare le eccezioni
    private static final Logger LOGGER = Logger.getLogger(VoteReviewServlet.class.getName());

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
        // Configurazione standard risposta AJAX/Voto
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            // FIX: Usiamo getSession(true) per combaciare perfettamente con lo stub del test
            final HttpSession session = request.getSession(true); 
            final UtenteBean user = (session != null) ? (UtenteBean) session.getAttribute("user") : null;

            // 1. Controllo Autenticazione
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                try {
                    // Prevenzione proattiva smell su getWriter()
                    response.getWriter().write("Devi essere autenticato per votare una recensione.");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di autorizzazione", e);
                }
                return; // Impedisce il crash o la prosecuzione senza utente
            }

            // 2. Recupero e Validazione parametri
            final String idFilmStr = request.getParameter("idFilm");
            final String emailRecensore = request.getParameter("emailRecensore");
            final String valutazioneStr = request.getParameter("valutazione");

            if (idFilmStr == null || emailRecensore == null || valutazioneStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int idFilm = Integer.parseInt(idFilmStr);
            boolean valutazione = Boolean.parseBoolean(valutazioneStr);

            // 3. Esecuzione Business Logic (Ora viene chiamata perché l'utente è trovato)
            RecensioniService.addValutazione(user.getEmail(), idFilm, emailRecensore, valutazione);
            
            // 4. Successo
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            // Gestione dependability dello smell IOException su sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore critico imprevisto.");
                } catch (IOException ioEx) {
                    // Sostituito il commento "Silenzioso" con un log reale
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore 500, stream disconnesso", ioEx);
                }
            }
        }
    }
}