package sottosistemi.Gestione_Utenti.view;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;

@WebServlet("/ModificaPreferenzeServlet")
public class ModificaPreferenzeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Risolto: Campo reso final e inizializzato direttamente per eliminare init()
    private final ProfileService profileService = new ProfileService();

    public ModificaPreferenzeServlet() {
        super();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession();
        final UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

        // 1. Controllo Autenticazione: L'utente è loggato?
        if (utenteSessione == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        final String targetEmail = request.getParameter("email"); 
        final String[] generiSelezionati = request.getParameterValues("selectedGenres");

        // 2. Controllo Autorizzazione: Chi fa la richiesta è il proprietario dell'account?
        // Utilizziamo un approccio "safe" per il confronto delle stringhe
        if (targetEmail != null && !targetEmail.equals(utenteSessione.getEmail())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare le preferenze di questo utente.");
            return;
        }

        final String email = utenteSessione.getEmail();

        // 3. Chiama il Service
        profileService.aggiornaPreferenzeUtente(email, generiSelezionati);
            
        // Aggiorna messaggio di successo in sessione
        session.setAttribute("messaggioSuccesso", "Preferenze aggiornate con successo!");

        // 4. Redirect al profilo
        response.sendRedirect("profile?visitedUser=" + utenteSessione.getUsername());
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("profile.jsp");
    }
}