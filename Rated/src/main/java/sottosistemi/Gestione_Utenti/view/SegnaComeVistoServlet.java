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

    // Risolto: Campi resi final e inizializzati direttamente
    private final ProfileService profileService = new ProfileService();
    private final RecensioniService recensioniService = new RecensioniService();

    public SegnaComeVistoServlet() {
        super();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");

            final HttpSession session = request.getSession();
            final UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

            if (utenteSessione == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // Risoluzione dello smell: gestione IOException per getWriter
                response.getWriter().write("Utente non loggato.");
                return;
            }

            final String filmIdStr = request.getParameter("filmId");
            int filmId = -1; 
            
            try {
                if (filmIdStr != null && !filmIdStr.isEmpty()) {
                    filmId = Integer.parseInt(filmIdStr);
                }
            } catch (final NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("ID Film non valido.");
                return;
            }

            if (filmId != -1) {
                final boolean giaVisto = profileService.isFilmVisto(utenteSessione.getEmail(), filmId);

                if (giaVisto) {
                    // Controllo se esiste recensione prima di rimuovere
                    final RecensioneBean recensione = recensioniService.getRecensione(filmId, utenteSessione.getEmail());

                    if (recensione != null) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                        response.getWriter().write("Non puoi rimuovere il film dai 'Visti' perché hai scritto una recensione. Elimina prima la recensione.");
                        return;
                    } else {
                        profileService.rimuoviFilmVisto(utenteSessione.getEmail(), filmId);
                    }
                } else {
                    profileService.aggiungiFilmVisto(utenteSessione.getEmail(), filmId);
                }

                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Film ID mancante.");
            }
            
        } catch (IOException e) {
            // Gestione dell'errore di sistema: invio di un codice di errore 500 se la risposta non è già stata inviata
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Si è verificato un errore durante l'aggiornamento dello stato 'visto'.");
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Risoluzione dello smell: gestione IOException per sendRedirect
            response.sendRedirect("catalogo.jsp");
        } catch (IOException e) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il reindirizzamento.");
            }
        }
    }
}