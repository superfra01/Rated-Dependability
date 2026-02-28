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

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.UtenteDAO != null;
    public AutenticationService() {
        this.UtenteDAO = new UtenteDAO();
    }

    //@ requires utenteDAO != null;
    //@ ensures this.UtenteDAO == utenteDAO;
    public AutenticationService(final UtenteDAO utenteDAO) { // Parametro final
        this.UtenteDAO = utenteDAO;
    }

    /* =========================================
     * METODI SERVICE
     * ========================================= */

    //@ requires email != null;
    //@ requires password != null;
    //@ assignable \everything;
    public UtenteBean login(final String email, final String password) { // Parametri final
        final UtenteBean user = UtenteDAO.findByEmail(email); // Variabile locale final
        
        // Aggiungiamo un check di sicurezza per la stringa hash, utile per la static verification
        if (user != null) {
            final String hash = PasswordUtility.hashPassword(password);
            if (hash != null && hash.equals(user.getPassword())) {
                return user; // Authentication successful
            }
        }
        
        return null; // Authentication failed
    }


    //@ requires session != null;
    //@ assignable \everything;
    public void logout(final HttpSession session) { // Parametro final
        session.invalidate();
    }
    
    
    //@ requires username != null;
    //@ requires email != null;
    //@ requires password != null;
    //@ requires biografia != null;
    //@ assignable \everything;
    //@ ensures \result != null ==> (\result.getEmail().equals(email) && \result.getUsername().equals(username));
    public UtenteBean register(final String username, final String email, final String password, final String biografia, final byte[] icon) { // Parametri final
        
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
        String hashedPwd = PasswordUtility.hashPassword(password);
        if (hashedPwd == null) {
            hashedPwd = ""; 
        }
        User.setPassword(hashedPwd);
        
        User.setTipoUtente("RECENSORE");
        User.setIcona(icon);
        User.setNWarning(0);
        User.setBiografia(biografia);
        
        UtenteDAO.save(User);
        
        return User;
    }
}