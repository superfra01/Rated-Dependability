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
    
    // Risolto: field final e inizializzato direttamente
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession(true);

        // Risolto: Estratto il parametro in una variabile final
        final String queryRicerca = request.getParameter("filmCercato");
        final List<FilmBean> films = catalogoService.ricercaFilm(queryRicerca);
        
        session.setAttribute("films", films);
        
        request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp").forward(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }
}