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

    // Risolto: Service resi final e inizializzati alla dichiarazione
    private final RecensioniService recensioniService = new RecensioniService();
    private final ProfileService profileService = new ProfileService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        
        final HttpSession session = request.getSession(true);
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        
        // Buona pratica: Controllo di sicurezza se l'utente è loggato
        if (user != null) {
            final int idFilm = Integer.parseInt(request.getParameter("idFilm"));
            final String titolo = request.getParameter("titolo");
            final String recensione = request.getParameter("recensione");
            final int valutazione = Integer.parseInt(request.getParameter("valutazione"));

            // Interazione con i Service (ora final)
            recensioniService.addRecensione(user.getEmail(), idFilm, recensione, titolo, valutazione);
            
            if (!profileService.isFilmVisto(user.getEmail(), idFilm)) {
                profileService.aggiungiFilmVisto(user.getEmail(), idFilm);
            }
            
            response.sendRedirect(request.getContextPath() + "/film?idFilm=" + idFilm);
        } else {
            // Se l'utente non è loggato, reindirizza alla login o mostra errore
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}