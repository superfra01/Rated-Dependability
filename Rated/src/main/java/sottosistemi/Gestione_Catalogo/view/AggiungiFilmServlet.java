package sottosistemi.Gestione_Catalogo.view;

import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final CatalogoService catalogoService = new CatalogoService();
    // Inizializzazione del Logger per gestire correttamente le eccezioni
    private static final Logger LOGGER = Logger.getLogger(AggiungiFilmServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {}

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession(true);
        final UtenteBean user = (UtenteBean) session.getAttribute("user");
        
        if (user != null && "GESTORE".equals(user.getTipoUtente())) {
            try {
                final int anno = Integer.parseInt(request.getParameter("annoFilm"));
                final int durata = Integer.parseInt(request.getParameter("durataFilm"));
                final String attori = request.getParameter("attoriFilm");
                final String[] generiSelezionati = request.getParameterValues("generiFilm");
                final String nome = request.getParameter("nomeFilm");
                final String regista = request.getParameter("registaFilm");
                final String trama = request.getParameter("tramaFilm");
                
                byte[] locandina = null; 
                try {
                    final Part filePart = request.getPart("locandinaFilm");
                    if (filePart != null && filePart.getSize() > 0) {
                        try (final InputStream inputStream = filePart.getInputStream()) {
                            locandina = inputStream.readAllBytes();
                        }
                    }
                } catch (IOException | ServletException e) {
                    if (!response.isCommitted()) response.sendError(500);
                    return;
                }

                catalogoService.addFilm(anno, attori, durata, generiSelezionati, locandina, nome, regista, trama);
                response.sendRedirect(request.getContextPath() + "/catalogo");
                
            } catch (NumberFormatException e) {
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    // Gestione dello smell getWriter
                    try {
                        response.getWriter().write("Errore: I campi 'Anno' e 'Durata' devono essere numeri validi.");
                    } catch (IOException ioException) {
                        LOGGER.log(Level.SEVERE, "Impossibile scrivere la risposta di errore per formato numero non valido", ioException);
                    }
                }
            } catch (Exception e) {
                if (!response.isCommitted()) response.sendError(500, "Si è verificato un errore critico imprevisto.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Gestione dello smell getWriter
            try {
                response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Impossibile scrivere la risposta di errore per utente non autorizzato", ioException);
            }
        }
    }
}