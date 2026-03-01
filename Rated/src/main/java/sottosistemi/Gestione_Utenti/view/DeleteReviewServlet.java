package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DeleteReview")
public class DeleteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Risolto: Campi resi final e inizializzati immediatamente
    // Naming mantenuto identico per non rompere i test di integrazione
    private final ProfileService ProfileService = new ProfileService();
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
            
            // Verifica che l'utente sia loggato prima di procedere
            if (user != null) {
                try {
                    final String email = user.getEmail();
                    
                    // Risoluzione dello smell: gestione NumberFormatException per DeleteFilmID
                    final int idFilm = Integer.parseInt(request.getParameter("DeleteFilmID"));

                    RecensioniService.deleteRecensione(email, idFilm);

                    // Risoluzione dello smell: gestione dell'eccezione IOException lanciata dal sendRedirect
                    response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + user.getUsername());
                    
                } catch (NumberFormatException e) {
                    // Gestione dell'errore se l'ID film non è un numero valido
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Errore: L'ID del film deve essere un valore numerico valido.");
                }
            } else {
                // Se l'utente non è loggato, reindirizza alla login
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
        } catch (IOException e) {
            // Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante la cancellazione della recensione.");
            }
        }
    }
}