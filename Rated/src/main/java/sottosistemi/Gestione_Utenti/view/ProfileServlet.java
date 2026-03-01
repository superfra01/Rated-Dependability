package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
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

    // Campi final e inizializzati direttamente per garantire thread-safety e immutabilità
    private final ProfileService profileService = new ProfileService(); 
    private final RecensioniService recensioniService = new RecensioniService();
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // 1. Protezione globale del flusso
        try {
            final HttpSession session = request.getSession(true);
            final String userName = request.getParameter("visitedUser");

            // 2. Controllo parametro di input
            if (userName == null || userName.isEmpty()) {
                handleSafeError(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'visitedUser' mancante.");
                return;
            }

            // 3. Recupero Utente (Dato Critico)
            final UtenteBean visitedUser = profileService.findByUsername(userName);
            
            if (visitedUser != null) {
                session.setAttribute("visitedUser", visitedUser);

                // 4. Recupero Dati Correlati (Isolamento dei Guasti)
                // Usiamo blocchi try-catch interni o fallback per dati non critici
                try {
                    final List<RecensioneBean> recensioni = recensioniService.FindRecensioni(visitedUser.getEmail());
                    session.setAttribute("recensioni", (recensioni != null) ? recensioni : new ArrayList<>());

                    final HashMap<Integer, FilmBean> filmMap = catalogoService.getFilms(recensioni);
                    session.setAttribute("films", (filmMap != null) ? filmMap : new HashMap<Integer, FilmBean>());
                } catch (Exception e) {
                    // Se fallisce il recupero recensioni, inizializziamo a vuoto per non rompere la JSP
                    session.setAttribute("recensioni", new ArrayList<>());
                    session.setAttribute("films", new HashMap<Integer, FilmBean>());
                }

                try {
                    final List<String> generi = catalogoService.getAllGeneri();
                    session.setAttribute("allGenres", (generi != null) ? generi : new ArrayList<>());

                    final List<String> userGenres = profileService.getPreferenze(visitedUser.getEmail());
                    session.setAttribute("userGenres", (userGenres != null) ? userGenres : new ArrayList<>());
                } catch (Exception e) {
                    session.setAttribute("allGenres", new ArrayList<>());
                    session.setAttribute("userGenres", new ArrayList<>());
                }
                
                // 5. Inoltro alla JSP protetto
                try {
                    request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(request, response);    
                } catch (ServletException | IOException e) {
                    handleCriticalError(response, "Errore interno durante il caricamento della vista profilo.");
                }

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("You can't access the profile page if visitedUser is not set");
            }

        } catch (Exception e) {
            // Catch-all per prevenire crash del thread e risolvere lo smell sendError
            handleCriticalError(response, "Si è verificato un errore critico imprevisto nel sistema.");
        }
    }

    /**
     * Helper per gestire errori critici gestendo l'eccezione IOException di sendError.
     * Risolve lo smell: "Handle the following exception that could be thrown by 'sendError': IOException."
     */
    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ioEx) {
                // Stream compromesso (es. client disconnesso), non possiamo fare altro
            }
        }
    }

    /**
     * Helper per scrivere errori testuali in modo sicuro, gestendo IOException di getWriter.
     */
    private void handleSafeError(HttpServletResponse response, int statusCode, String message) {
        try {
            if (!response.isCommitted()) {
                response.setStatus(statusCode);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(message);
            }
        } catch (IOException e) {
            // Connessione interrotta dal client
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}