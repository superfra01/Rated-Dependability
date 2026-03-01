package sottosistemi.Gestione_Catalogo.view;

import model.Entity.FilmBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ricerca")
public class RicercaCatalogoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Field final e inizializzato per garantire l'immutabilità del service
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // 1. Protezione globale del flusso
        try {
            final HttpSession session = request.getSession(true);

            // 2. Recupero e sanificazione base del parametro (Null-safe)
            String queryRicerca = request.getParameter("filmCercato");
            if (queryRicerca == null) {
                queryRicerca = ""; // Evita NullPointerException nel service
            }

            // 3. Interazione con il Service protetta
            final List<FilmBean> films;
            try {
                films = catalogoService.ricercaFilm(queryRicerca);
            } catch (Exception e) {
                // Se la ricerca fallisce a livello DB, gestiamo l'errore senza crashare
                handleCriticalError(response, "Errore durante l'interrogazione del catalogo.");
                return;
            }

            // 4. Gestione degli attributi
            // Nota: Se la lista è vuota, la JSP dovrà gestire il feedback "Nessun risultato"
            session.setAttribute("films", films);

            // 5. Inoltro alla vista (Forward) con gestione delle eccezioni
            try {
                request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp").forward(request, response);
            } catch (ServletException | IOException e) {
                handleCriticalError(response, "Errore interno durante il caricamento della vista dei risultati.");
            }

        } catch (Exception e) {
            // Catch-all per prevenire falle di sicurezza o crash imprevisti
            handleCriticalError(response, "Si è verificato un errore critico imprevisto.");
        }
    }

    /**
     * Metodo helper per gestire risposte di errore in modo "dependable".
     * Risolve lo smell: "Handle the following exception that could be thrown by 'sendError': IOException."
     */
    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                // Impostiamo il tipo di contenuto prima dell'errore per coerenza
                response.setContentType("text/plain;charset=UTF-8");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ioException) {
                // Se anche sendError fallisce (connessione interrotta), non possiamo fare altro che terminare
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Supportiamo la ricerca anche via POST per flessibilità, delegando al GET
        doGet(request, response);
    }
}