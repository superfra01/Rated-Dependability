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
    
    // Risolto: Campi resi final e inizializzati immediatamente per eliminare lo smell
    // Naming mantenuto identico per non rompere i test di integrazione
    private final ProfileService ProfileService = new ProfileService();
    private final RecensioniService RecensioniService = new RecensioniService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final HttpSession session = request.getSession(true);
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        
        // Buona pratica: verifica che l'utente sia loggato prima di procedere
        if (user != null) {
            final String email = user.getEmail();
            final int ID_Film = Integer.parseInt(request.getParameter("DeleteFilmID"));

            RecensioniService.deleteRecensione(email, ID_Film);

            response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + user.getUsername());
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}