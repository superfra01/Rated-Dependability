package benchmark;

import model.DAO.UtenteDAO;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import utilities.PasswordUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class ProfileServiceBenchmark {

    private ProfileService service;
    
    // Dati per i test
    private List<RecensioneBean> listaRecensioni;
    private final String EMAIL_TEST = "mario.rossi@test.com";
    private final String USERNAME_TEST = "SuperMario";
    private final String PASSWORD_TEST = "PasswordSicura123";

    @Setup(Level.Trial)
    public void setup() {
        // 1. Creiamo il Mock DAO usando il costruttore "sicuro" (true)
        final UtenteDAO mockDao = new UtenteDAO(true) { // <--- IMPORTANTE: true
            @Override
            public UtenteBean findByEmail(String email) {
                final UtenteBean u = new UtenteBean();
                u.setEmail(email);
                u.setUsername("User_" + email);
                u.setPassword("oldHash"); 
                return u;
            }

            @Override
            public UtenteBean findByUsername(String username) {
                return null; // Username libero
            }

            @Override
            public void update(UtenteBean user) {
                // Non fa nulla
            }
        };

        // 2. INIEZIONE TRAMITE COSTRUTTORE
        this.service = new ProfileService(mockDao);

        // 3. Prepariamo la lista per testare il ciclo getUsers
        this.listaRecensioni = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final RecensioneBean r = new RecensioneBean();
            r.setEmail("user" + i + "@test.com"); // Email diverse
            listaRecensioni.add(r);
        }
    }

    @Benchmark
    public void testProfileUpdate(Blackhole bh) {
        final UtenteBean u = service.ProfileUpdate(USERNAME_TEST, EMAIL_TEST, PASSWORD_TEST, "Nuova Bio", null);
        bh.consume(u);
    }

    @Benchmark
    public void testPasswordUpdate(Blackhole bh) {
        final UtenteBean u = service.PasswordUpdate(EMAIL_TEST, PASSWORD_TEST);
        bh.consume(u);
    }

    @Benchmark
    public void testGetUsersMap(Blackhole bh) {
        final HashMap<String, String> map = service.getUsers(listaRecensioni);
        bh.consume(map);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(ProfileServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}