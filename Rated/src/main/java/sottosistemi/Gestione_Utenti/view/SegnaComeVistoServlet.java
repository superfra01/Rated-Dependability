package sottosistemi.Gestione_Utenti.view;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;
import sottosistemi.Gestione_Utenti.service.ProfileService;

@WebServlet("/SegnaComeVistoServlet")
public class SegnaComeVistoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public SegnaComeVistoServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

        if (utenteSessione == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Utente non loggato.");
            return;
        }

        String filmIdStr = request.getParameter("filmId");
        int filmId = -1;
        try {
            if (filmIdStr != null && !filmIdStr.isEmpty()) {
                filmId = Integer.parseInt(filmIdStr);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ID Film non valido.");
            return;
        }

        if (filmId != -1) {
            ProfileService profileService = new ProfileService();
            RecensioniService recensioniService = new RecensioniService();
            
            boolean giaVisto = profileService.isFilmVisto(utenteSessione.getEmail(), filmId);
            
            if (giaVisto) {
                // Controllo se esiste recensione prima di rimuovere
                RecensioneBean recensione = recensioniService.getRecensione(filmId, utenteSessione.getEmail());
                
                if (recensione != null) {
                    // ERRORE: Non si può rimuovere dai visti se c'è una recensione
                    response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                    response.getWriter().write("Non puoi rimuovere il film dai 'Visti' perché hai scritto una recensione. Elimina prima la recensione.");
                    return;
                } else {
                    profileService.rimuoviFilmVisto(utenteSessione.getEmail(), filmId);
                }
            } else {
                profileService.aggiungiFilmVisto(utenteSessione.getEmail(), filmId);
            }
            
            // Successo
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Film ID mancante.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("catalogo.jsp");
    }
}