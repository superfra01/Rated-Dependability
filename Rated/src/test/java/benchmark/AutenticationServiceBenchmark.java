package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.mockito.Mockito;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;
import utilities.PasswordUtility;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class AutenticationServiceBenchmark {

    private AutenticationService autenticationService;
    private UtenteDAO mockUtenteDAO;
    private HttpSession mockSession;

    // Variabili di test
    private final String testEmail = "utente@test.com";
    private final String testEmailInesistente = "nessuno@test.com";
    private final String testUsername = "TestUser";
    private final String testNewUsername = "NewUser";
    private final String testPassword = "SuperSecretPassword123";
    private final String testWrongPassword = "WrongPassword";
    private final String testBio = "Biografia di prova";
    private final byte[] testIcon = new byte[0];

    private UtenteBean mockUser;

    @Setup(Level.Trial)
    public void setUp() {
        // 1. Inizializzazione mock in modalità stubOnly
        mockUtenteDAO = Mockito.mock(UtenteDAO.class, Mockito.withSettings().stubOnly());
        mockSession = Mockito.mock(HttpSession.class, Mockito.withSettings().stubOnly());

        // 2. Iniezione nel Service
        autenticationService = new AutenticationService(mockUtenteDAO);

        // 3. Preparazione dei dati di test
        mockUser = new UtenteBean();
        mockUser.setEmail(testEmail);
        mockUser.setUsername(testUsername);
        
        // Attenzione: Per far sì che il login abbia successo nel test, la password nel mock 
        // deve corrispondere all'hash che PasswordUtility genererà durante il benchmark.
        String hashedPassword = PasswordUtility.hashPassword(testPassword);
        mockUser.setPassword(hashedPassword);

        // 4. Configurazione dei comportamenti del mock
        // Caso: Utente Esistente
        Mockito.when(mockUtenteDAO.findByEmail(testEmail)).thenReturn(mockUser);
        Mockito.when(mockUtenteDAO.findByUsername(testUsername)).thenReturn(mockUser);
        
        // Caso: Utente Non Esistente (per far funzionare correttamente la registrazione)
        Mockito.when(mockUtenteDAO.findByEmail("nuovo@test.com")).thenReturn(null);
        Mockito.when(mockUtenteDAO.findByUsername(testNewUsername)).thenReturn(null);
        Mockito.when(mockUtenteDAO.findByEmail(testEmailInesistente)).thenReturn(null);
    }

    @Benchmark
    public void benchmarkLoginSuccess(Blackhole bh) {
        // Scenario: L'utente esiste e la password è corretta
        bh.consume(autenticationService.login(testEmail, testPassword));
    }

    @Benchmark
    public void benchmarkLoginWrongPassword(Blackhole bh) {
        // Scenario: L'utente esiste ma la password è errata
        bh.consume(autenticationService.login(testEmail, testWrongPassword));
    }

    @Benchmark
    public void benchmarkLoginUserNotFound(Blackhole bh) {
        // Scenario: L'utente non esiste nel database (ramo più veloce)
        bh.consume(autenticationService.login(testEmailInesistente, testPassword));
    }

    @Benchmark
    public void benchmarkLogout(Blackhole bh) {
        // Scenario: Invalidazione della sessione
        autenticationService.logout(mockSession);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkRegisterSuccess(Blackhole bh) {
        // Scenario: L'email e l'username non esistono, la registrazione procede
        bh.consume(autenticationService.register(testNewUsername, "nuovo@test.com", testPassword, testBio, testIcon));
    }

    @Benchmark
    public void benchmarkRegisterEmailAlreadyExists(Blackhole bh) {
        // Scenario: Fallimento rapido perché l'email è già in uso
        bh.consume(autenticationService.register(testNewUsername, testEmail, testPassword, testBio, testIcon));
    }
    
    @Benchmark
    public void benchmarkRegisterUsernameAlreadyExists(Blackhole bh) {
        // Scenario: Fallimento al secondo controllo perché l'username è già in uso
        bh.consume(autenticationService.register(testUsername, "nuovo@test.com", testPassword, testBio, testIcon));
    }
}