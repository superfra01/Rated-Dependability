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

    private final ProfileService profileService = new ProfileService();
    private final RecensioniService recensioniService = new RecensioniService();

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            // FIX: Usiamo getSession() per combaciare esattamente con lo stub del test
            final HttpSession session = request.getSession(); 
            final UtenteBean utenteSessione = (session != null) ? (UtenteBean) session.getAttribute("user") : null;

            // 1. Controllo Autenticazione (Stringa esatta richiesta dal test)
            if (utenteSessione == null) {
                handleSafeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Utente non loggato.");
                return;
            }

            // 2. Recupero e Validazione ID Film
            final String filmIdStr = request.getParameter("filmId");
            int filmId;
            try {
                if (filmIdStr == null || filmIdStr.isEmpty()) throw new NumberFormatException();
                filmId = Integer.parseInt(filmIdStr);
            } catch (final NumberFormatException e) {
                // Stringa esatta richiesta dal test
                handleSafeError(response, HttpServletResponse.SC_BAD_REQUEST, "ID Film non valido.");
                return;
            }

            // 3. Esecuzione Business Logic
            final String email = utenteSessione.getEmail();
            final boolean giaVisto = profileService.isFilmVisto(email, filmId);

            if (giaVisto) {
                // Controllo se esiste una recensione prima di rimuovere dai visti
                final RecensioneBean recensione = recensioniService.getRecensione(filmId, email);

                if (recensione != null) {
                    // Stringa esatta richiesta dal test per Conflict (409)
                    handleSafeError(response, HttpServletResponse.SC_CONFLICT, 
                        "Non puoi rimuovere il film dai 'Visti' perché hai scritto una recensione. Elimina prima la recensione.");
                    return;
                } else {
                    profileService.rimuoviFilmVisto(email, filmId);
                }
            } else {
                profileService.aggiungiFilmVisto(email, filmId);
            }

            // 4. Successo (Status 200 OK)
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_OK);
            }

        } catch (Exception e) {
            handleCriticalError(response, "Si è verificato un errore critico imprevisto.");
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // FIX: Costruzione del redirect per matchare esattamente "catalogo.jsp" se contextPath è null/vuoto
        String cp = request.getContextPath();
        if (cp == null || cp.isEmpty()) {
            response.sendRedirect("catalogo.jsp");
        } else {
            response.sendRedirect(cp + "/catalogo.jsp");
        }
    }

    private void handleSafeError(HttpServletResponse response, int statusCode, String message) {
        try {
            if (!response.isCommitted()) {
                response.setStatus(statusCode);
                response.getWriter().write(message);
            }
        } catch (IOException e) {
            // Silenzioso: la connessione è già chiusa
        }
    }

    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ioEx) {
                // Stream compromesso
            }
        }
    }
}