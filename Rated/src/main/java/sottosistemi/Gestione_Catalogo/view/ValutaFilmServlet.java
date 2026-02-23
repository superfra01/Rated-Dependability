package sottosistemi.Gestione_Catalogo.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ValutaFilm")
public class ValutaFilmServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private RecensioniService RecensioniService;
	private ProfileService ProfileService;

    @Override
    public void init() {
        RecensioniService = new RecensioniService();
        ProfileService = new ProfileService();
        
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { // Parametri final
      
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { // Parametri final
    	
    	final HttpSession session = request.getSession(true); // Locale final
    	final UtenteBean user = (UtenteBean) session.getAttribute("user"); // Locale final
    	final int idFilm = Integer.parseInt(request.getParameter("idFilm")); // Locale final
    	final String titolo = request.getParameter("titolo"); // Locale final
    	final String recensione = request.getParameter("recensione"); // Locale final
    	final int valutazione = Integer.parseInt(request.getParameter("valutazione")); // Locale final

    	RecensioniService.addRecensione(user.getEmail(), idFilm, recensione, titolo, valutazione);
    	if(ProfileService.isFilmVisto(user.getEmail(), idFilm)==false)
    		ProfileService.aggiungiFilmVisto(user.getEmail(), idFilm);
    	
    	
    	response.sendRedirect(request.getContextPath() + "/film?idFilm="+idFilm);
    }
}