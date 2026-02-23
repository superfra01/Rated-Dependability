package sottosistemi.Gestione_Utenti.service;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import utilities.PasswordUtility;

import java.sql.SQLException;

import javax.servlet.http.HttpSession;

public class AutenticationService {
    private final UtenteDAO UtenteDAO; // Reso final

    public AutenticationService() {
        this.UtenteDAO = new UtenteDAO();
    }

    public AutenticationService(final UtenteDAO utenteDAO) { // Parametro final
        this.UtenteDAO = utenteDAO;
    }

    public UtenteBean login(final String email, final String password) { // Parametri final
        final UtenteBean user = UtenteDAO.findByEmail(email); // Variabile locale final
        if (user != null && PasswordUtility.hashPassword(password).equals(user.getPassword())) {
            return user; // Authentication successful
        }
        
        return null; // Authentication failed
    }

    public void logout(final HttpSession session) { // Parametro final
        session.invalidate();
    }
    
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
        User.setPassword(PasswordUtility.hashPassword(password));
        User.setTipoUtente("RECENSORE");
        User.setIcona(icon);
        User.setNWarning(0);
        User.setBiografia(biografia);
        
        UtenteDAO.save(User);
        
        return User;
    }
}