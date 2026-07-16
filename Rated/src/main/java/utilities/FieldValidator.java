package utilities;

import java.util.regex.Pattern;

public class FieldValidator {

    /*@ spec_public @*/
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,30}$");
    
    /*@ spec_public @*/
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,64}$");
    
    /*@ spec_public @*/
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /* =========================================
     * METODI DI VALIDAZIONE
     * ========================================= */

    /*@ 
      @ public normal_behavior
      @ assignable \nothing;
      @ ensures \result <==> username != null
      @     && 3 <= username.length() && username.length() <= 30
      @     && (\forall int i; 0 <= i && i < username.length();
      @            ('a' <= username.charAt(i) && username.charAt(i) <= 'z')
      @         || ('A' <= username.charAt(i) && username.charAt(i) <= 'Z')
      @         || ('0' <= username.charAt(i) && username.charAt(i) <= '9')
      @         || username.charAt(i) == '_' || username.charAt(i) == '.');
      @ skipesc
      @ skiprac
      @*/
    public static boolean validateUsername(final /*@ nullable @*/ String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /*@ 
      @ public normal_behavior
      @ assignable \nothing;
      @ ensures \result <==> password != null
      @     && 8 <= password.length() && password.length() <= 64
      @     && (\forall int i; 0 <= i && i < password.length();
      @            ('a' <= password.charAt(i) && password.charAt(i) <= 'z')
      @         || ('A' <= password.charAt(i) && password.charAt(i) <= 'Z')
      @         || ('0' <= password.charAt(i) && password.charAt(i) <= '9')
      @         || password.charAt(i) == '@' || password.charAt(i) == '$'
      @         || password.charAt(i) == '!' || password.charAt(i) == '%'
      @         || password.charAt(i) == '*' || password.charAt(i) == '?'
      @         || password.charAt(i) == '&' || password.charAt(i) == '.')
      @     && !(\forall int i; 0 <= i && i < password.length();
      @            !('a' <= password.charAt(i) && password.charAt(i) <= 'z'))
      @     && !(\forall int i; 0 <= i && i < password.length();
      @            !('A' <= password.charAt(i) && password.charAt(i) <= 'Z'))
      @     && !(\forall int i; 0 <= i && i < password.length();
      @            !('0' <= password.charAt(i) && password.charAt(i) <= '9'))
      @     && !(\forall int i; 0 <= i && i < password.length();
      @           !(password.charAt(i) == '@' || password.charAt(i) == '$'
      @         || password.charAt(i) == '!' || password.charAt(i) == '%'
      @         || password.charAt(i) == '*' || password.charAt(i) == '?'
      @         || password.charAt(i) == '&' || password.charAt(i) == '.'));
      @ skipesc
      @ skiprac
      @*/
    public static boolean validatePassword(final /*@ nullable @*/ String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /*@ 
      @ public normal_behavior
      @ assignable \nothing;
      @ ensures \result <==> email != null
      @     && !(\forall int at, dot;
      @            0 <= at && at < email.length() && 0 <= dot && dot < email.length();
      @           !(0 < at && at + 1 < dot && dot + 2 < email.length()
      @         && email.charAt(at) == '@' && email.charAt(dot) == '.'
      @         && (\forall int i; 0 <= i && i < at;
      @                ('a' <= email.charAt(i) && email.charAt(i) <= 'z')
      @             || ('A' <= email.charAt(i) && email.charAt(i) <= 'Z')
      @             || ('0' <= email.charAt(i) && email.charAt(i) <= '9')
      @             || email.charAt(i) == '.' || email.charAt(i) == '_'
      @             || email.charAt(i) == '%' || email.charAt(i) == '+'
      @             || email.charAt(i) == '-')
      @         && (\forall int i; at < i && i < dot;
      @                ('a' <= email.charAt(i) && email.charAt(i) <= 'z')
      @             || ('A' <= email.charAt(i) && email.charAt(i) <= 'Z')
      @             || ('0' <= email.charAt(i) && email.charAt(i) <= '9')
      @             || email.charAt(i) == '.' || email.charAt(i) == '-')
      @         && (\forall int i; dot < i && i < email.length();
      @                ('a' <= email.charAt(i) && email.charAt(i) <= 'z')
      @             || ('A' <= email.charAt(i) && email.charAt(i) <= 'Z'))));
      @ skipesc
      @ skiprac
      @*/
    public static boolean validateEmail(final /*@ nullable @*/ String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
