package sottosistemi.Gestione_Catalogo.view;

import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/catalogo")
public class VisualizzaCatalogoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Risolto: field reso final e inizializzato subito
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession(true);

        final List<FilmBean> films = catalogoService.getFilms();
        session.setAttribute("films", films);
        
        // Risolto: anche la variabile del ciclo for può essere final
        for (final FilmBean film : films) {
            final List<FilmGenereBean> generi = catalogoService.getGeneri(film.getIdFilm());
            session.setAttribute(film.getIdFilm() + "Generi", generi);
        }
            
        request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp").forward(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }
}