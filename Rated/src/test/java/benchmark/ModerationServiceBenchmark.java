package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.mockito.Mockito;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ModerationService;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class ModerationServiceBenchmark {

    private ModerationService moderationService;
    private UtenteDAO mockUtenteDAO;

    // Dati di test
    private final String testEmail = "utente.segnalato@email.com";
    private UtenteBean mockUser;

    @Setup(Level.Trial)
    public void setUp() {
        // 1. Inizializza il mock in modalità "stubOnly" per massime performance in JMH
        mockUtenteDAO = Mockito.mock(UtenteDAO.class, Mockito.withSettings().stubOnly());

        // 2. Inietta il mock tramite il costruttore del service dedicato ai test
        moderationService = new ModerationService(mockUtenteDAO);

        // 3. Configura il comportamento di default del mock
        mockUser = new UtenteBean();
        mockUser.setEmail(testEmail);
        mockUser.setNWarning(0); // Partiamo da 0 warning

        // Quando il service cerca l'utente tramite email, restituisci il nostro mockUser
        Mockito.when(mockUtenteDAO.findByEmail(testEmail)).thenReturn(mockUser);
    }

    @Benchmark
    public void benchmarkWarn(Blackhole bh) {
        // Eseguiamo il metodo da testare
        moderationService.warn(testEmail);
        
        // Consumiamo un token per evitare che il compilatore JIT ottimizzi via l'intera chiamata (Dead Code Elimination)
        bh.consume(true);
    }
    
    @Benchmark
    public void benchmarkWarnUserNotFound(Blackhole bh) {
        // Testiamo anche il caso in cui l'utente non viene trovato (user == null)
        // Utile per misurare il path di esecuzione più breve (il blocco if viene saltato)
        moderationService.warn("email.inesistente@email.com");
        bh.consume(false);
    }
}