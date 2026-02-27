package utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtility {

    /*@ 
      @ requires password != null;
      @ ensures \result != null;
      @ assignable \nothing;
      @ skiprac
      @*/
    public static String hashPassword(final String password) { 
        // Nota: il salt è codificato nel metodo. In JML consideriamo getBytes() 
        // come una funzione che non altera lo stato visibile dell'oggetto.
        final byte[] salt = "salatino".getBytes();
        
        try {
            // SHA-256
            final MessageDigest md = MessageDigest.getInstance("SHA-256"); 
            
            md.update(salt);
            
            // Il punto critico per OpenJML: getBytes() e digest() invocano logiche native
            final byte[] hashedPassword = md.digest(password.getBytes()); 
            
            final byte[] hashWithSalt = new byte[salt.length + hashedPassword.length]; 
            System.arraycopy(salt, 0, hashWithSalt, 0, salt.length);
            System.arraycopy(hashedPassword, 0, hashWithSalt, salt.length, hashedPassword.length);
            
            // La conversione in Base64 restituisce una stringa non nulla (ensures \result != null)
            return Base64.getEncoder().encodeToString(hashWithSalt);
        } catch (final NoSuchAlgorithmException e) {
            // Gestione dell'eccezione come runtime in caso di configurazione errata del sistema
            throw new RuntimeException("Errore durante la creazione dell'hash: " + e.getMessage());
        }
    }
}