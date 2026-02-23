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
	
	private ProfileService ProfileService; 

    @Override
    public void init() {
    	ProfileService = new ProfileService();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    	final HttpSession session = request.getSession(true);
    	final String userName = request.getParameter("visitedUser");
    	
    	final UtenteBean visitedUser = ProfileService.findByUsername(userName);
        if(visitedUser!=null) {
        	session.setAttribute("visitedUser", visitedUser);
        	
        	final RecensioniService RecensioniService = new RecensioniService();
        	final List<RecensioneBean> recensioni = RecensioniService.FindRecensioni(visitedUser.getEmail());
        	session.setAttribute("recensioni", recensioni);
        	
        	final CatalogoService CatalogoService = new CatalogoService();
        	final HashMap<Integer, FilmBean> FilmMap = CatalogoService.getFilms(recensioni);
        	session.setAttribute("films", FilmMap);
        
        	CatalogoService catalogoService = new CatalogoService();
        	List<String> generi = catalogoService.getAllGeneri();
        	session.setAttribute("allGenres", generi);
        	
        	List<String> userGenres = ProfileService.getPreferenze(visitedUser.getEmail());
        	session.setAttribute("userGenres", userGenres);
        	request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(request, response);	
        } else {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("You can't access the profile page if visitedUser is not set");
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    	
    }
}