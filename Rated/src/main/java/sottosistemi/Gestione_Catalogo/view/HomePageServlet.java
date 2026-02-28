package sottosistemi.Gestione_Catalogo.view;

import java.io.IOException;
import java.util.ArrayList;
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
    
    // Risolto: il service può essere final se inizializzato subito
    private final CatalogoService catalogoService = new CatalogoService();

    public HomePageServlet() {
        super();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession(); // Locale final
        
        // Recupera l'utente dalla sessione
        final UtenteBean utente = (UtenteBean) session.getAttribute("user"); // Locale final
        
        List<FilmBean> filmConsigliati = null; // Non può essere final perché viene riassegnata nell'if
        
        // Se l'utente è loggato, calcola i consigliati
        if (utente != null) {
            // La Servlet interagisce SOLO con il Service
            filmConsigliati = catalogoService.getFilmCompatibili(utente);
        }

        // Carica la lista in sessione
        session.setAttribute("filmConsigliati", filmConsigliati);

        // Forward alla HomePage
        request.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp").forward(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}