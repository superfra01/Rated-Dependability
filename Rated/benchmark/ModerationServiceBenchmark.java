package benchmark;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ModerationService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class ModerationServiceBenchmark {

    private ModerationService service;

    private final String EMAIL_ESISTENTE = "baduser@test.com";
    private final String EMAIL_INESISTENTE = "ghost@test.com";

    @Setup(Level.Trial)
    public void setup() {
        final UtenteDAO mockDao = new UtenteDAO(true) { 
            @Override
            public UtenteBean findByEmail(String email) {
                if ("baduser@test.com".equals(email)) { 
                    final UtenteBean u = new UtenteBean();
                    u.setEmail(email);
                    u.setNWarning(0);
                    return u;
                }
                return null;
            }

            @Override
            public void update(UtenteBean u) {
                // Non fa nulla
            }
        };

        this.service = new ModerationService(mockDao);
    }

    @Benchmark
    public void testWarnUserExists() {
        service.warn(EMAIL_ESISTENTE);
    }

    @Benchmark
    public void testWarnUserNotFound() {
        service.warn(EMAIL_INESISTENTE);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(ModerationServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}