package sottosistemi.Gestione_Utenti.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/passwordModify")
public class PasswordModifyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ProfileService ProfileService;

    @Override
    public void init() {
        ProfileService = new ProfileService();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    	
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String email = request.getParameter("email");
        final String password = request.getParameter("password");
        
        if(FieldValidator.validatePassword(password)) {
        	final UtenteBean utente = ProfileService.PasswordUpdate(email, password);
        	
        	final HttpSession session = request.getSession(true);
        	session.setAttribute("user", utente);
        	
        	response.sendRedirect(request.getContextPath() + "/profile?visitedUser=" + ((UtenteBean)session.getAttribute("user")).getUsername());
        }
    }
}