package utilities;

import java.util.regex.Pattern;

public class FieldValidator {

    // 1. Pre-compilazione dei pattern (static final)
    //    Questo sposta il costo di compilazione all'avvio della classe, non durante l'uso.
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,30}$");
    
    //    La Regex password è invariata, ma ora è compilata una volta sola.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$");
    
    //    Regex email standard
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // Validazione username
    public static boolean validateUsername(final String username) {
        if (username == null) return false;
        // 2. Riutilizzo del pattern compilato per creare il Matcher
        return USERNAME_PATTERN.matcher(username).matches();
    }

    // Validazione password
    public static boolean validatePassword(final String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    // Validazione email
    public static boolean validateEmail(final String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}