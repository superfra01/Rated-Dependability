package sottosistemi.Gestione_Utenti.view;

import model.DAO.GenereDAO;
import model.Entity.InteresseBean;
import model.Entity.PreferenzaBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import utilities.FieldValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/register")
@MultipartConfig(maxFileSize = 16177215)
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1879879L;
	private AutenticationService authService;
	private ProfileService profService;

    @Override
    public void init() {
        authService = new AutenticationService();
        profService = new ProfileService();
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    	final HttpSession session = req.getSession(true);
    	CatalogoService catalogoService = new CatalogoService();
    	List<String> generi = catalogoService.getAllGeneri();
    	session.setAttribute("genres", generi);
    	req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
        
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    	final String username = request.getParameter("username");
    	final String email = request.getParameter("email");
    	final String password = request.getParameter("password");
    	final String confirmPassword = request.getParameter("confirm_password");
    	final String biography = request.getParameter("biography");
    	
    	final String[] generi= request.getParameterValues("genres");
    	
    	byte[] icon = null; // Riassegnata
    	
    	final Part filePart = request.getPart("profile_icon");
        if (filePart != null && filePart.getSize() > 0) {
             try (final InputStream inputStream = filePart.getInputStream()) {
                 icon = inputStream.readAllBytes();
             }
        }

        if (FieldValidator.validateUsername(username) &&
            FieldValidator.validateEmail(email) &&
            FieldValidator.validatePassword(password) &&
            password.equals(confirmPassword)) {

            final UtenteBean utente = authService.register(username, email, password, biography, icon);
            
            for(String genere : generi) {
            	profService.addPreferenza(email, genere);
            	
            }

            if (utente != null) {
                response.sendRedirect(request.getContextPath() + "/login"); // Redirect to login after successful registration
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Registration failed. User may already exist.");
            }

        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid form data. Check your inputs.");
        }
    }
}