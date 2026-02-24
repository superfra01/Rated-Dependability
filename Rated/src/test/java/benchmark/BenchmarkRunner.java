package benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Runner principale per eseguire i benchmark JMH.
 * Modifica la classe nel metodo include() per eseguire benchmark diversi.
 *
 * Per eseguire: mvn clean compile test-compile exec:java
 */
public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
             // .include(AutenticationServiceBenchmark.class.getSimpleName())
               //  .include(CatalogoServiceBenchmark.class.getSimpleName())
               // .include(ModerationServiceBenchmark.class.getSimpleName())
                // .include(ProfileServiceBenchmark.class.getSimpleName())
                .include(RecensioniServiceBenchmark.class.getSimpleName())
                .forks(2)
                .build();

        new Runner(opt).run();
    }
}
