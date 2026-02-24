package benchmark;

import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
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
    private UtenteBean utenteTest;
    private String[] generiTest;

    @Setup(Level.Trial)
    public void setup() {
        // 1. MOCK FILM DAO
        final FilmDAO mockFilmDao = new FilmDAO() {
            @Override
            public List<FilmBean> findAll() {
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
                final FilmBean f = new FilmBean();
                f.setIdFilm(id);
                f.setNome("Film Trovato " + id);
                return f;
            }

            @Override
            public List<FilmBean> findByName(String name) {
                final List<FilmBean> list = new ArrayList<>();
                final FilmBean f = new FilmBean();
                f.setIdFilm(1);
                f.setNome(name);
                list.add(f);
                return list;
            }

            @Override
            public List<FilmBean> doRetrieveConsigliati(String email) {
                final List<FilmBean> list = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    final FilmBean f = new FilmBean();
                    f.setIdFilm(i);
                    list.add(f);
                }
                return list;
            }

            @Override public void save(FilmBean f) {}
            @Override public void update(FilmBean f) {}
            @Override public void delete(int id) {}
        };

        // 2. MOCK FILMGENERE DAO
        final FilmGenereDAO mockFilmGenereDao = new FilmGenereDAO() {
            @Override
            public List<FilmGenereBean> findByIdFilm(int id) {
                final List<FilmGenereBean> list = new ArrayList<>();
                list.add(new FilmGenereBean(id, "Azione"));
                return list;
            }
            @Override public void save(FilmGenereBean fg) {}
            @Override public void deleteByIdFilm(int id) {}
        };

        // 3. MOCK GENERE DAO
        final GenereDAO mockGenereDao = new GenereDAO() {
            @Override
            public List<String> findAllString() {
                List<String> list = new ArrayList<>();
                list.add("Azione");
                list.add("Commedia");
                return list;
            }
        };

        // 4. INIEZIONE NEL SERVICE
        this.service = new CatalogoService(mockFilmDao, mockFilmGenereDao, mockGenereDao);

        // 5. PREPARAZIONE DATI PER I TEST
        this.listaRecensioniTest = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final RecensioneBean r = new RecensioneBean();
            r.setIdFilm(i); 
            listaRecensioniTest.add(r);
        }

        this.nomeDaCercare = "Matrix";
        
        this.utenteTest = new UtenteBean();
        this.utenteTest.setEmail("test@email.com");
        
        this.generiTest = new String[]{"Azione", "Fantascienza", "Thriller"};
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
    
    @Benchmark
    public void testGetFilmCompatibili(Blackhole bh) {
        final List<FilmBean> result = service.getFilmCompatibili(utenteTest);
        bh.consume(result);
    }

    @Benchmark
    public void testAddFilm(Blackhole bh) {
        // Test per valutare le performance del ciclo di inserimento generi
        service.addFilm(1999, "Keanu Reeves", 136, generiTest, new byte[0], "Matrix", "Wachowski", "Trama test");
        bh.consume(true);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(CatalogoServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}