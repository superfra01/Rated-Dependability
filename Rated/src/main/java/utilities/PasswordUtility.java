package utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtility {
	// Metodo per creare l'hash della password con il salt
		public static String hashPassword(final String password) { // Parametro final
	    	final byte[] salt = "salatino".getBytes();
	    	
	        try {
	            // Creazione dell'istanza del MessageDigest con SHA-256
	            final MessageDigest md = MessageDigest.getInstance("SHA-256"); // Locale final
	            
	            // Aggiunge il salt
	            md.update(salt);
	            
	            // Hash della password
	            final byte[] hashedPassword = md.digest(password.getBytes()); // Locale final
	            
	            // Combina il salt e l'hash della password
	            final byte[] hashWithSalt = new byte[salt.length + hashedPassword.length]; // Locale final
	            System.arraycopy(salt, 0, hashWithSalt, 0, salt.length);
	            System.arraycopy(hashedPassword, 0, hashWithSalt, salt.length, hashedPassword.length);
	            
	            // Converte il risultato in una stringa Base64
	            return Base64.getEncoder().encodeToString(hashWithSalt);
	        } catch (final NoSuchAlgorithmException e) {
	            throw new RuntimeException("Errore durante la creazione dell'hash: " + e.getMessage());
	        }
	    }
}