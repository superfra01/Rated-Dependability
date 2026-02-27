package utilities;

/**
 * Questa classe contiene deliberatamente vulnerabilità di tipo "Hardcoded Password/Secret"
 * al solo scopo di testare la corretta rilevazione da parte della pipeline CI con Snyk Code.
 */
public class SnykHardcodedTest {

    // ESEMPIO 1: Password del database inserita direttamente come costante
    private static final String DB_PASSWORD = "SuperSecretPassword123!";
    
    // ESEMPIO 2: Token per una presunta API
    private static final String API_TOKEN = "12345abcdef67890-test-token";

    public void connectToSystem() {
        String username = "admin";
        // ESEMPIO 3: Password hardcoded in una variabile locale
        String password = "MyHardcodedPassword456"; 

        System.out.println("Tentativo di connessione con l'utente: " + username);
        
        // Finta logica di connessione
        if (password.equals("MyHardcodedPassword456")) {
            System.out.println("Connessione effettuata con successo usando DB_PASSWORD: " + DB_PASSWORD);
        }
    }
}