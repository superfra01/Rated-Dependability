package sottosistemi.Gestione_Utenti.view;

import java.io.IOException;
import java.sql.SQLException;

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

    public ModificaPreferenzeServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UtenteBean utenteSessione = (UtenteBean) session.getAttribute("user");

        // 1. Controllo Autenticazione: L'utente è loggato?
        if (utenteSessione == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Recupera i parametri dal form
        // Supponiamo che il form invii un campo nascosto 'targetUsername' o usiamo direttamente la sessione
        String targetEmail = request.getParameter("email"); 
        
        // Recupera i generi selezionati (checkboxes con name="generi")
        String[] generiSelezionati = request.getParameterValues("selectedGenres");

        // 2. Controllo Autorizzazione: Chi fa la richiesta è il proprietario dell'account?
        // Se targetUsername è null, assumiamo che l'utente voglia modificare se stesso (fallback sicuro)
        if (targetEmail != null && !targetEmail.equals(utenteSessione.getEmail())) {
            // Tentativo di IDOR (Insecure Direct Object Reference) o modifica non autorizzata
            // Se sei un admin potresti bypassare questo controllo, ma per ora blocchiamo.
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a modificare le preferenze di questo utente.");
            return;
        }

        // Usa lo username della sessione per sicurezza massima se il parametro è vuoto
        String email =  utenteSessione.getEmail();

        // 3. Chiama il Service
        ProfileService profileService = new ProfileService();
        
        profileService.aggiornaPreferenzeUtente(email, generiSelezionati);
            
        // Aggiorna messaggio di successo in sessione
        request.getSession().setAttribute("messaggioSuccesso", "Preferenze aggiornate con successo!");
            
        
           

        // 4. Redirect al profilo
        response.sendRedirect("profile?visitedUser="+utenteSessione.getUsername()); // O "profile.jsp" se non passi per una servlet di visualizzazione
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Redirige eventuali accessi diretti via URL
        response.sendRedirect("profile.jsp");
    }
}