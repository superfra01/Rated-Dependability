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
	private CatalogoService CatalogoService;

    @Override
    public void init() {
        CatalogoService = new CatalogoService();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { // Parametri final
    	final HttpSession session = request.getSession(true); // Locale final

    	final List<FilmBean> films = CatalogoService.ricercaFilm(request.getParameter("filmCercato")); // Locale final
    	session.setAttribute("films", films);
    	
        request.getRequestDispatcher("/WEB-INF/jsp/catalogo.jsp").forward(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { // Parametri final
    	
    }
}