package sottosistemi.Gestione_Recensioni.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ApproveReview")
public class ApproveReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Naming mantenuto identico per compatibilità con il field injection del test
    private final RecensioniService RecensioniService = new RecensioniService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        String cp = request.getContextPath();
        response.sendRedirect((cp != null ? cp : "") + "/moderator");
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Recupero Sessione: Usiamo getSession(true) per combaciare con lo stub del test
            final HttpSession session = request.getSession(true); 
            final UtenteBean user = (session != null) ? (UtenteBean) session.getAttribute("user") : null;
            
            // 2. Controllo Autorizzazione (Messaggio esatto richiesto dal test)
            if (user == null || !"MODERATORE".equals(user.getTipoUtente())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
                return; // Interrompe l'esecuzione prevenendo il cascade al 500
            }

            // 3. Recupero e Validazione parametri
            final String userEmail = request.getParameter("ReviewUserEmail");
            final String idFilmStr = request.getParameter("idFilm");
            
            if (userEmail == null || idFilmStr == null || idFilmStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int idFilm;
            try {
                idFilm = Integer.parseInt(idFilmStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 4. Esecuzione Business Logic
            RecensioniService.deleteReports(userEmail, idFilm);

            // 5. Redirect finale (Sincronizzato con il "Wanted" del test: /Rated/moderator)
            if (!response.isCommitted()) {
                String cp = request.getContextPath();
                response.sendRedirect((cp != null ? cp : "") + "/moderator");
            }

        } catch (Exception e) {
            // Risoluzione smell SonarCloud: gestione IOException di sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(500, "Si è verificato un errore critico imprevisto.");
                } catch (IOException ioEx) {
                    // Silenzioso: la connessione è già chiusa
                }
            }
        }
    }
}