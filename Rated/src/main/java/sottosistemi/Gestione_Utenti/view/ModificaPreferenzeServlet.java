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
    
    // VARIABILE DI ISTANZA
    private ProfileService profileService;

    public ModificaPreferenzeServlet() {
        super();
    }

    @Override
    public void init() {
        // Inizializzazione spostata qui
        profileService = new ProfileService();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

        // 1. Controllo Autenticazione: L'utente è loggato?
        if (utenteSessione == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String targetEmail = request.getParameter("email"); 
        String[] generiSelezionati = request.getParameterValues("selectedGenres");

        // 2. Controllo Autorizzazione: Chi fa la richiesta è il proprietario dell'account?
        if (targetEmail != null && !targetEmail.equals(utenteSessione.getEmail())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare le preferenze di questo utente.");
            return;
        }

        String email = utenteSessione.getEmail();

        // 3. Chiama il Service (ora usa la variabile di istanza)
        profileService.aggiornaPreferenzeUtente(email, generiSelezionati);
            
        // Aggiorna messaggio di successo in sessione
        request.getSession().setAttribute("messaggioSuccesso", "Preferenze aggiornate con successo!");

        // 4. Redirect al profilo
        response.sendRedirect("profile?visitedUser=" + utenteSessione.getUsername());
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("profile.jsp");
    }
}