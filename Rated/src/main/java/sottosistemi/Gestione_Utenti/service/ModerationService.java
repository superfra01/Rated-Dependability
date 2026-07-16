package sottosistemi.Gestione_Utenti.service;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;

public class ModerationService {
    
    public final UtenteDAO UtenteDAO; // Reso final

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant UtenteDAO != null;
    //@ public invariant UtenteDAO.dataSource != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.UtenteDAO != null;
    //@ assignable \nothing;
    public ModerationService() {
        this.UtenteDAO = new UtenteDAO();
    }
    
    // Costruttore per il test
    //@ requires utenteDAO != null;
    //@ requires utenteDAO.dataSource != null;
    //@ ensures this.UtenteDAO == utenteDAO;
    //@ assignable \nothing;
    public ModerationService(final UtenteDAO utenteDAO) { // Parametro final
        this.UtenteDAO = utenteDAO;
    }
    
    /* =========================================
     * METODI SERVICE
     * ========================================= */

    //@ requires email != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void warn(final String email) { // Parametro final
        final /*@ nullable @*/ UtenteBean user = UtenteDAO.findByEmail(email); // Variabile locale final
        if(user != null) {
            user.setNWarning(user.getNWarning() + 1);
            UtenteDAO.update(user);
        }
    }
}
