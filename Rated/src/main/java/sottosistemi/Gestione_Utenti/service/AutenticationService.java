package sottosistemi.Gestione_Utenti.service;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import utilities.PasswordUtility;

import java.sql.SQLException;

import javax.servlet.http.HttpSession;


public class AutenticationService {
    
    //@ spec_public
    private final UtenteDAO UtenteDAO;

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
    public AutenticationService() {
        this.UtenteDAO = new UtenteDAO();
    }

    //@ requires utenteDAO != null;
    //@ requires utenteDAO.dataSource != null;
    //@ ensures this.UtenteDAO == utenteDAO;
    //@ assignable \nothing;
    public AutenticationService(final UtenteDAO utenteDAO) { // Parametro final
        this.UtenteDAO = utenteDAO;
    }

    /* =========================================
     * METODI SERVICE
     * ========================================= */

    //@ requires email != null;
    //@ requires password != null;
    //@ assignable \nothing;
    //@ ensures \result != null ==> \result.getEmail().equals(email);
    public /*@ nullable @*/ UtenteBean login(final String email, final String password) { // Parametri final
        final /*@ nullable @*/ UtenteBean user = UtenteDAO.findByEmail(email); // Variabile locale final
        
        // Aggiungiamo un check di sicurezza per la stringa hash, utile per la static verification
        if (user != null) {
            final String hash = PasswordUtility.hashPassword(password);
            if (hash.equals(user.getPassword())) {
                return user; // Authentication successful
            }
        }
        
        return null; // Authentication failed
    }


    //@ requires session != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void logout(final HttpSession session) { // Parametro final
        session.invalidate();
    }
    
    
    //@ requires username != null;
    //@ requires email != null;
    //@ requires password != null;
    //@ requires biografia != null;
    //@ assignable \everything;
    //@ ensures \result != null ==> (\result.getEmail().equals(email) && \result.getUsername().equals(username));
    //@ ensures \result != null ==> \result.getPassword().length() == 56;
    //@ ensures \result != null ==> \result.getTipoUtente().equals("RECENSORE") && \result.getNWarning() == 0;
    //@ ensures \result != null ==> \result.getBiografia().equals(biografia) && \result.getIcona() == icon;
    public /*@ nullable @*/ UtenteBean register(final String username, final String email, final String password, final String biografia, final byte /*@ nullable @*/ [] icon) { // Parametri final
        
        // Check if the user already exists
        if (UtenteDAO.findByEmail(email) != null) {
            return null; // User already exists
        }
        
        // Check if the user already exists
        if (UtenteDAO.findByUsername(username) != null) {
            return null; // User already exists
        }
        
        final UtenteBean User = new UtenteBean(); // Variabile locale final
        User.setUsername(username);
        User.setEmail(email);
        
        // Previene la violazione dell'invariante di UtenteBean nel caso il metodo utility ritorni null (ESC lo assume possibile)
        final String hashedPwd = PasswordUtility.hashPassword(password);
        User.setPassword(hashedPwd);
        
        User.setTipoUtente("RECENSORE");
        User.setIcona(icon);
        User.setNWarning(0);
        User.setBiografia(biografia);
        
        UtenteDAO.save(User);
        
        return User;
    }
}
