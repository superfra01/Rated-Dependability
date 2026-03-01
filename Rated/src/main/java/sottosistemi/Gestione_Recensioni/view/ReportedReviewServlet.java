package sottosistemi.Gestione_Recensioni.view;

import model.Entity.UtenteBean;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;

import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/moderator")
public class ReportedReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Naming con iniziale maiuscola mantenuto per compatibilità con i campi riflessi dei Test
    private final CatalogoService CatalogoService = new CatalogoService();
    private final RecensioniService RecensioniService = new RecensioniService();
    private final ProfileService ProfileService = new ProfileService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Recupero Sessione: Usiamo getSession(true) per combaciare con lo stub del test
            final HttpSession session = request.getSession(true); 
            final UtenteBean user = (session != null) ? (UtenteBean) session.getAttribute("user") : null;

            // 2. Controllo Autorizzazione (Messaggio e Status esatti per i test)
            if (user != null && "MODERATORE".equals(user.getTipoUtente())) {

                // 3. Recupero Dati
                final List<RecensioneBean> recensioni = RecensioniService.GetAllRecensioniSegnalate();
                session.setAttribute("recensioni", recensioni);

                final HashMap<String, String> utenti = ProfileService.getUsers(recensioni);
                session.setAttribute("users", utenti);

                final HashMap<Integer, FilmBean> filmMap = CatalogoService.getFilms(recensioni);
                session.setAttribute("films", filmMap);

                // 4. Forward alla JSP
                request.getRequestDispatcher("/WEB-INF/jsp/moderator.jsp").forward(request, response);

            } else {
                // RIPRISTINO: Status 400 e stringa originale come richiesto dal test
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("You can't access the profile page unless you are an authenticated moderator.");
                return; // Interrompe l'esecuzione
            }
            
        } catch (Exception e) {
            // Risoluzione dello smell: gestione IOException di sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante il caricamento della pagina moderatore.");
                } catch (IOException ioEx) {
                    // Silenzioso: la connessione è già chiusa
                }
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}