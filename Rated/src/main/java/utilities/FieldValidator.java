package utilities;

import java.util.regex.Pattern;

public class FieldValidator {

    /*@ spec_public @*/
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,30}$");
    
    /*@ spec_public @*/
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$");
    
    /*@ spec_public @*/
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /* =========================================
     * METODI DI VALIDAZIONE
     * ========================================= */

    /*@ 
      @ assignable \nothing;
      @ skiprac
      @*/
    public static boolean validateUsername(final String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /*@ 
      @ assignable \nothing;
      @ skiprac
      @*/
    public static boolean validatePassword(final String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /*@ 
      @ assignable \nothing;
      @ skiprac
      @*/
    public static boolean validateEmail(final String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}