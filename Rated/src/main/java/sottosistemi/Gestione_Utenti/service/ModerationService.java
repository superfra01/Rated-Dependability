package sottosistemi.Gestione_Utenti.service;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;

public class ModerationService {
    public final UtenteDAO UtenteDAO; // Reso final

    public ModerationService() {
        this.UtenteDAO = new UtenteDAO();
    }
    
    // Costruttore per il test
    public ModerationService(final UtenteDAO utenteDAO) { // Parametro final
        this.UtenteDAO = utenteDAO;
    }
    
    public void warn(final String email) { // Parametro final
    	final UtenteBean user = UtenteDAO.findByEmail(email); // Variabile locale final
    	if(user != null) {
    		user.setNWarning(user.getNWarning() + 1);
        	UtenteDAO.update(user);
    	}
    }
}