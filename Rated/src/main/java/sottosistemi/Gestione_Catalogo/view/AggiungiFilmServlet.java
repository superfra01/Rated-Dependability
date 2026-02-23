package sottosistemi.Gestione_Catalogo.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/addFilm")
@MultipartConfig(maxFileSize = 16177215)
public class AggiungiFilmServlet extends HttpServlet {
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
    		
    		final int anno = Integer.parseInt(request.getParameter("annoFilm")); // Locale final
    		final String Attori = request.getParameter("attoriFilm"); // Locale final
    		final int durata = Integer.parseInt(request.getParameter("durataFilm")); // Locale final
    		final String[] generiSelezionati = request.getParameterValues("generiFilm");
    		final String Nome = request.getParameter("nomeFilm"); // Locale final
    		final String Regista = request.getParameter("registaFilm"); // Locale final
    		final String Trama = request.getParameter("tramaFilm"); // Locale final
    		
    		byte[] locandina = null; // Non può essere final perché riassegnata
        	
       	 	final Part filePart = request.getPart("locandinaFilm"); // Locale final
            if (filePart != null && filePart.getSize() > 0) {
                try (final InputStream inputStream = filePart.getInputStream()) { // Risorsa final
                    locandina = inputStream.readAllBytes();
                }
            }

    		CatalogoService.addFilm(anno, Attori, durata, generiSelezionati, locandina, Nome, Regista, Trama);
    		response.sendRedirect(request.getContextPath() + "/catalogo");
    	}else {
    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
    	}
    }
}