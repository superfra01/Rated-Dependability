package sottosistemi.Gestione_Catalogo.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/deleteFilm")
public class RimuoviFilmServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private CatalogoService CatalogoService;

    @Override
    public void init() {
        CatalogoService = new CatalogoService();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { // Parametri final
      
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { // Parametri final
    	
    	final HttpSession session = request.getSession(true); // Locale final
    	final UtenteBean user = (UtenteBean) session.getAttribute("user"); // Locale final
    	if(user.getTipoUtente().equals("GESTORE")) {
    		
    		final int idFilm = Integer.parseInt(request.getParameter("idFilm")); // Locale final
    		
    		CatalogoService.removeFilm(idFilm);
    		//response.sendRedirect(request.getContextPath() + "/catalogo");
    		response.sendRedirect(request.getContextPath() + "/catalogo");
    		
    	}else {
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
    	}
    }
}