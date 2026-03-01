package sottosistemi.Gestione_Catalogo.view;

import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/catalogo")
public class VisualizzaCatalogoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Field final per garantire l'immutabilità e thread-safety del service
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        
        // 1. Protezione globale del flusso
        try {
            final HttpSession session = request.getSession(true);

            // 2. Recupero dati dal Service con gestione null-safe
            List<FilmBean> films;
            try {
                films = catalogoService.getFilms();
                if (films == null) {
                    films = new ArrayList<>(); // Evita NullPointerException nel ciclo for e nella JSP
                }
            } catch (Exception e) {
                // Se il recupero dei film fallisce (es. DB down), gestiamo l'errore subito
                handleCriticalError(response, "Impossibile recuperare il catalogo dei film.");
                return;
            }

            session.setAttribute("films", films);
            
            // 3. Recupero Generi (Logica di arricchimento dati)
            for (final FilmBean film : films) {
                if (film != null) {
                    try {
                        final List<FilmGenereBean> generi = catalogoService.getGeneri(film.getIdFilm());
                        // Usiamo una chiave univoca basata sull'ID per evitare collisioni in sessione
                        session.setAttribute(film.getIdFilm() + "Generi", (generi != null) ? generi : new ArrayList<>());
                    } catch (Exception e) {
                        // Se fallisce il recupero dei generi per un singolo film, non blocchiamo l'intero catalogo
                        session.setAttribute(film.getIdFilm() + "Generi", new ArrayList<>());
                    }
                }
            }

            // 4. Inoltro alla vista (Forward) protetto
            try {
                request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp").forward(request, response);
            } catch (ServletException | IOException e) {
                handleCriticalError(response, "Errore interno durante il caricamento della pagina catalogo.");
            }

        } catch (Exception e) {
            // Catch-all per RuntimeException impreviste (es. OutOfMemory o errori di sessione)
            handleCriticalError(response, "Si è verificato un errore critico nel sistema.");
        }
    }

    /**
     * Metodo helper per gestire le risposte di errore in modo "dependable".
     * Risolve lo smell: "Handle the following exception that could be thrown by 'sendError': IOException."
     */
    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.setContentType("text/plain;charset=UTF-8");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ioEx) {
                // Se anche sendError fallisce (connessione interrotta), il thread termina senza lanciare altre eccezioni
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Delega al GET per supportare refresh o redirect POST-Redirect-GET
        doGet(request, response);
    }
}