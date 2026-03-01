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
    
    // Campo reso final e inizializzato direttamente per eliminare init()
    // Mantengo il nome "RecensioniService" per compatibilità con i test esistenti
    private final RecensioniService RecensioniService = new RecensioniService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession(true);
            final UtenteBean user = (UtenteBean) session.getAttribute("user");
            
            // Verifica dei permessi del moderatore
            if (user != null && "MODERATORE".equals(user.getTipoUtente())) {
                try {
                    final String userEmail = request.getParameter("ReviewUserEmail");
                    
                    // Risoluzione dello smell: gestione NumberFormatException per idFilm
                    final int idFilm = Integer.parseInt(request.getParameter("idFilm"));

                    RecensioniService.deleteReports(userEmail, idFilm);

                    // Risoluzione dello smell: gestione dell'eccezione IOException lanciata dal sendRedirect
                    response.sendRedirect(request.getContextPath() + "/moderator");
                    
                } catch (NumberFormatException e) {
                    // Gestione dell'errore se l'ID film non è un numero valido
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Errore: L'ID del film deve essere un valore numerico valido.");
                }
            } else {
                // Gestione mancanza di autorizzazione
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
            }
        } catch (IOException e) {
            // Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante l'approvazione della recensione.");
            }
        }
    }
}