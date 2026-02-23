package sottosistemi.Gestione_Catalogo.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Entity.FilmBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

@WebServlet("")
public class HomePageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public HomePageServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        // Recupera l'utente dalla sessione
        UtenteBean utente = (UtenteBean) session.getAttribute("user");
        
        List<FilmBean> filmConsigliati = null;
        
        // Se l'utente è loggato, calcola i consigliati
        if (utente != null) {
            CatalogoService service = new CatalogoService();
            // La Servlet interagisce SOLO con il Service, mai con il DAO direttamente
            filmConsigliati = service.getFilmCompatibili(utente);
        } else {
            // Opzionale: Se l'utente non è loggato, potresti voler mostrare i film più popolari generici
            // o lasciare la lista vuota. Qui la lasciamo vuota o gestiamo diversamente.
        }

        // Carica la lista in sessione come richiesto
        session.setAttribute("filmConsigliati", filmConsigliati);

        // Forward alla HomePage (il percorso corrisponde alla tua struttura file)
        request.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}