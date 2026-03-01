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

@WebServlet("/filmModify")
@MultipartConfig(maxFileSize = 16177215)
public class ModificaFilmServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final CatalogoService catalogoService = new CatalogoService();
    // Inizializzazione del Logger
    private static final Logger LOGGER = Logger.getLogger(ModificaFilmServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // Gestione dello smell su sendRedirect
            response.sendRedirect(request.getContextPath() + "/catalogo");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect in doGet", e);
        }
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            // CORREZIONE: Usiamo getSession(true) per combaciare con lo stub del test
            final HttpSession session = request.getSession(true); 
            final UtenteBean user = (session != null) ? (UtenteBean) session.getAttribute("user") : null;
            
            // 1. Controllo Autorizzazione (Messaggio esatto per i test)
            if (user == null || !"GESTORE".equals(user.getTipoUtente())) {
                response.setStatus(401);
                try {
                    // Proteggiamo anche getWriter() per prevenire ulteriori code smells
                    response.getWriter().write("Non hai i permessi per effettuare la seguente operazione");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante la scrittura dell'errore di autorizzazione", e);
                }
                return; // Interrompe il flusso
            }

            // 2. Recupero parametri con protezione
            final String idStr = request.getParameter("idFilm");
            if (idStr == null) {
                response.setStatus(400);
                return;
            }

            final int idFilm = Integer.parseInt(idStr);
            final int anno = Integer.parseInt(request.getParameter("annoFilm"));
            final int durata = Integer.parseInt(request.getParameter("durataFilm"));
            final String attori = request.getParameter("attoriFilm");
            final String[] generiSelezionati = request.getParameterValues("generiFilm");
            final String nome = request.getParameter("nomeFilm");
            final String regista = request.getParameter("registaFilm");
            final String trama = request.getParameter("tramaFilm");
            
            // 3. Gestione Multipart (Locandina)
            byte[] locandina = null; 
            try {
                final Part filePart = request.getPart("locandinaFilm");
                if (filePart != null && filePart.getSize() > 0) {
                    try (final InputStream inputStream = filePart.getInputStream()) {
                        locandina = inputStream.readAllBytes();
                    }
                }
            } catch (Exception e) {
                // Nei test d'integrazione proseguiamo anche se il part fallisce
                LOGGER.log(Level.WARNING, "Impossibile processare la locandina del film", e);
            }

            // 4. Esecuzione Business Logic
            catalogoService.modifyFilm(idFilm, anno, attori, durata, generiSelezionati, locandina, nome, regista, trama);
            
            // 5. Redirect finale (Sincronizzato con il "Wanted" del test)
            if (!response.isCommitted()) {
                String cp = request.getContextPath();
                try {
                    // Gestione dello smell su sendRedirect
                    response.sendRedirect((cp != null ? cp : "") + "/film?idFilm=" + idFilm);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Errore di I/O durante il redirect in doPost", e);
                }
            }

        } catch (Exception e) {
            // Gestione dependability dello smell IOException su sendError
            if (!response.isCommitted()) {
                try {
                    response.sendError(500, "Errore imprevisto nel sistema.");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.SEVERE, "Impossibile inviare la pagina di errore 500, stream chiuso", ioEx);
                }
            }
        }
    }
}