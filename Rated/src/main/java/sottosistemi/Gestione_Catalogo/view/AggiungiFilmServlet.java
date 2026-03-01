package sottosistemi.Gestione_Catalogo.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/addFilm")
@MultipartConfig(maxFileSize = 16177215)
public class AggiungiFilmServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    // Inizializzato direttamente e reso final
    private final CatalogoService catalogoService = new CatalogoService();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Metodo vuoto
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        
        final HttpSession session = request.getSession(true);
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        
        // Verifica dei permessi
        if (user != null && "GESTORE".equals(user.getTipoUtente())) {
            
            try {
                // Risoluzione dello smell: gestione NumberFormatException per anno e durata
                final int anno = Integer.parseInt(request.getParameter("annoFilm"));
                final int durata = Integer.parseInt(request.getParameter("durataFilm"));

                final String attori = request.getParameter("attoriFilm");
                final String[] generiSelezionati = request.getParameterValues("generiFilm");
                final String nome = request.getParameter("nomeFilm");
                final String regista = request.getParameter("registaFilm");
                final String trama = request.getParameter("tramaFilm");
                
                byte[] locandina = null; 
                final Part filePart = request.getPart("locandinaFilm");
                
                if (filePart != null && filePart.getSize() > 0) {
                    try (final InputStream inputStream = filePart.getInputStream()) {
                        locandina = inputStream.readAllBytes();
                    }
                }

                // Inserimento del film tramite il service
                catalogoService.addFilm(anno, attori, durata, generiSelezionati, locandina, nome, regista, trama);
                response.sendRedirect(request.getContextPath() + "/catalogo");
                
            } catch (NumberFormatException e) {
                // Gestione dell'errore nel caso i parametri numerici non siano validi
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Errore: I campi 'Anno' e 'Durata' devono essere numeri validi.");
            }
            
        } else {
            // Gestione mancanza di permessi
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
        }
    }
}