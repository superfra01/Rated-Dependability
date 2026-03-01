package sottosistemi.Gestione_Utenti.view;

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

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Campi resi final e inizializzati direttamente per eliminare init()
    private final ProfileService profileService = new ProfileService(); 
    private final RecensioniService recensioniService = new RecensioniService();
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession(true);
            final String userName = request.getParameter("visitedUser");
            
            final UtenteBean visitedUser = profileService.findByUsername(userName);
            
            if (visitedUser != null) {
                session.setAttribute("visitedUser", visitedUser);
                
                final List<RecensioneBean> recensioni = recensioniService.FindRecensioni(visitedUser.getEmail());
                session.setAttribute("recensioni", recensioni);
                
                final HashMap<Integer, FilmBean> filmMap = catalogoService.getFilms(recensioni);
                session.setAttribute("films", filmMap);
            
                // Variabili locali final
                final List<String> generi = catalogoService.getAllGeneri();
                session.setAttribute("allGenres", generi);
                
                final List<String> userGenres = profileService.getPreferenze(visitedUser.getEmail());
                session.setAttribute("userGenres", userGenres);
                
                // Risoluzione dello smell: gestione delle eccezioni ServletException e IOException lanciate dal forward
                request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(request, response);    

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // Risoluzione dello smell: gestione IOException per getWriter
                response.getWriter().write("You can't access the profile page if visitedUser is not set");
            }
        } catch (ServletException | IOException e) {
            // Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante il caricamento del profilo.");
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }
}