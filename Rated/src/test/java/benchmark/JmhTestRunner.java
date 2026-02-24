package benchmark;

import org.junit.jupiter.api.Test; 
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhTestRunner {

    @Test
    public void launchBenchmark() throws Exception {
        final Options opt = new OptionsBuilder()
            // Includi qui le classi di benchmark che vuoi eseguire
            .include(PasswordUtilityBenchmark.class.getSimpleName()) 
            // .include(AltroServiceBenchmark.class.getSimpleName())
            .forks(2) 
            .warmupIterations(15) 
            .measurementIterations(40)
            .build();

        new Runner(opt).run();
    }
}