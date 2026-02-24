package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import utilities.FieldValidator;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class FieldValidatorBenchmark {

    // --- DATI VALIDI ---
    private String usernameValid = "SuperMario_99";
    private String passwordValid = "PasswordSegreta1!";
    private String emailValid    = "mario.rossi@example.com";

    // --- DATI INVALIDI ---
    private String usernameInvalid = "A"; 
    private String passwordInvalid = "sololettere"; 
    private String emailInvalid    = "mariorossi.com"; 

    // ---------------- USERNAME ----------------
    @Benchmark
    public void testUsernameValid(Blackhole bh) {
        final boolean res = FieldValidator.validateUsername(usernameValid);
        bh.consume(res);
    }

    @Benchmark
    public void testUsernameInvalid(Blackhole bh) {
        final boolean res = FieldValidator.validateUsername(usernameInvalid);
        bh.consume(res);
    }

    // ---------------- PASSWORD ----------------
    @Benchmark
    public void testPasswordValid(Blackhole bh) {
        final boolean res = FieldValidator.validatePassword(passwordValid);
        bh.consume(res);
    }

    @Benchmark
    public void testPasswordInvalid(Blackhole bh) {
        final boolean res = FieldValidator.validatePassword(passwordInvalid);
        bh.consume(res);
    }

    // ---------------- EMAIL ----------------
    @Benchmark
    public void testEmailValid(Blackhole bh) {
        final boolean res = FieldValidator.validateEmail(emailValid);
        bh.consume(res);
    }

    @Benchmark
    public void testEmailInvalid(Blackhole bh) {
        final boolean res = FieldValidator.validateEmail(emailInvalid);
        bh.consume(res);
    }
}