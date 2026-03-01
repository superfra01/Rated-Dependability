package sottosistemi.Gestione_Catalogo.view;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

@WebServlet("")
public class HomePageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Inizializzato direttamente e reso final
    private final CatalogoService catalogoService = new CatalogoService();

    public HomePageServlet() {
        super();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final HttpSession session = request.getSession(); 
            
            // Recupera l'utente dalla sessione
            final UtenteBean utente = (UtenteBean) session.getAttribute("user"); 
            
            List<FilmBean> filmConsigliati = null; 

            // Se l'utente è loggato, calcola i consigliati
            if (utente != null) {
                // La Servlet interagisce SOLO con il Service
                filmConsigliati = catalogoService.getFilmCompatibili(utente);
            }

            // Carica la lista in sessione
            session.setAttribute("filmConsigliati", filmConsigliati);

            // Risoluzione dello smell: gestione delle eccezioni ServletException e IOException lanciate dal forward
            request.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp").forward(request, response);
            
        } catch (ServletException | IOException e) {
            // Gestione dell'errore: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante il caricamento della HomePage.");
            }
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}