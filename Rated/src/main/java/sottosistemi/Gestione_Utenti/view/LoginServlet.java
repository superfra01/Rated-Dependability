package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import utilities.FieldValidator;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final AutenticationService authService = new AutenticationService();

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        } catch (ServletException | IOException e) {
            handleCriticalError(resp, "Errore durante il caricamento della pagina di login.");
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final String email = request.getParameter("email");
            final String password = request.getParameter("password");

            // 1. Validazione Formato (Deve restituire "Errore di LogIn" per i test)
            if (email == null || password == null || 
                !FieldValidator.validateEmail(email) || 
                !FieldValidator.validatePassword(password)) {
                
                handleSafeForward(request, response, "/WEB-INF/jsp/login.jsp", "Errore di LogIn");
                return;
            }

            // 2. Esecuzione Login tramite Service
            final UtenteBean utente = authService.login(email, password);

            if (utente != null) {
                // 3. Successo: Usiamo getSession(true) per combaciare con lo stub del test
                final HttpSession session = request.getSession(true);
                session.setAttribute("user", utente);
                
                // Redirect alla home (gestendo il context path per i test)
                String cp = request.getContextPath();
                response.sendRedirect((cp != null ? cp : "") + "/");
            } else {
                // 4. Fallimento Credenziali: Deve restituire "Email o password non valide."
                handleSafeForward(request, response, "/WEB-INF/jsp/login.jsp", "Email o password non valide.");
            }

        } catch (Exception e) {
            handleCriticalError(response, "Si è verificato un errore critico durante l'autenticazione.");
        }
    }

    private void handleSafeForward(HttpServletRequest request, HttpServletResponse response, String path, String errorMsg) {
        try {
            if (!response.isCommitted()) {
                request.setAttribute("loginError", errorMsg);
                request.getRequestDispatcher(path).forward(request, response);
            }
        } catch (ServletException | IOException e) {
            // Fallback silenzioso
        }
    }

    private void handleCriticalError(HttpServletResponse response, String message) {
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ioEx) {
                // Connessione interrotta
            }
        }
    }
}