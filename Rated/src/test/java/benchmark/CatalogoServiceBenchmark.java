package benchmark;

import model.DAO.FilmDAO;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class CatalogoServiceBenchmark {

    private CatalogoService service;
    private List<RecensioneBean> listaRecensioniTest;
    private String nomeDaCercare;

    @Setup(Level.Trial)
    public void setup() {
        // 1. CREIAMO IL MOCK DI FILM DAO
        final FilmDAO mockDao = new FilmDAO(true) {
            
            @Override
            public List<FilmBean> findAll() {
                // Simuliamo una lista di 100 film
                final List<FilmBean> list = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    final FilmBean f = new FilmBean();
                    f.setIdFilm(i);
                    f.setNome("Film " + i);
                    list.add(f);
                }
                return list;
            }

            @Override
            public FilmBean findById(int id) {
                // Ritorniamo un film finto immediato
                final FilmBean f = new FilmBean();
                f.setIdFilm(id);
                f.setNome("Film Trovato " + id);
                return f;
            }

            @Override
            public List<FilmBean> findByName(String name) {
                // Simuliamo il risultato di una ricerca
                final List<FilmBean> list = new ArrayList<>();
                final FilmBean f = new FilmBean();
                f.setNome(name);
                list.add(f);
                return list;
            }

            // Override dei metodi di scrittura per non fare nulla
            @Override public void save(FilmBean f) {}
            @Override public void update(FilmBean f) {}
            @Override public void delete(int id) {}
        };

        // 2. INIEZIONE DEL MOCK NEL SERVICE
        this.service = new CatalogoService(mockDao);

        // 3. PREPARAZIONE DATI PER I TEST
        this.listaRecensioniTest = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final RecensioneBean r = new RecensioneBean();
            r.setIdFilm(i); 
            listaRecensioniTest.add(r);
        }

        this.nomeDaCercare = "Matrix";
    }

    @Benchmark
    public void testGetFilms(Blackhole bh) {
        final List<FilmBean> result = service.getFilms();
        bh.consume(result);
    }

    @Benchmark
    public void testGetFilmsByRecensioni(Blackhole bh) {
        final var result = service.getFilms(listaRecensioniTest);
        bh.consume(result);
    }

    @Benchmark
    public void testRicercaFilm(Blackhole bh) {
        final List<FilmBean> result = service.ricercaFilm(nomeDaCercare);
        bh.consume(result);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(CatalogoServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}