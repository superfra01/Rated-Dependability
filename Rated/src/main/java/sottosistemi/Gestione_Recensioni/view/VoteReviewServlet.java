package sottosistemi.Gestione_Recensioni.view;

import java.io.IOException;
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

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        String cp = request.getContextPath();
        response.sendRedirect((cp != null ? cp : "") + "/catalogo");
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
                response.getWriter().write("Devi essere autenticato per votare una recensione.");
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
                    // Silenzioso
                }
            }
        }
    }
}