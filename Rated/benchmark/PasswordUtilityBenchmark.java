package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import utilities.PasswordUtility;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class PasswordUtilityBenchmark {

    @Param({
        "123456", 
        "UnaPasswordMoltoLungaPerVedereSeCambiaQualcosaNelTempoDiHashing"
    })
    private String passwordToTest;

    @Benchmark
    public void testHashPassword(Blackhole bh) {
        final String result = PasswordUtility.hashPassword(passwordToTest);
        
        bh.consume(result);
    }
}