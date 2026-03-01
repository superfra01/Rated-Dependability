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
    
    // Risolto: Campo reso final e inizializzato direttamente
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { 
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException { 
        
        final HttpSession session = request.getSession(true);
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        
        // Buona pratica: controllo null sull'utente prima di accedere al tipo
        if (user != null && "GESTORE".equals(user.getTipoUtente())) {
            
            try {
                // Risoluzione dello smell: gestione NumberFormatException per idFilm
                final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
                
                catalogoService.removeFilm(idFilm);
                response.sendRedirect(request.getContextPath() + "/catalogo");

            } catch (NumberFormatException e) {
                // Gestione dell'errore se l'ID film non è un numero valido
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Errore: L'ID del film deve essere un valore numerico valido.");
            }

        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
        }
    }
}