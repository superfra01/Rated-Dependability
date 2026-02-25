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
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

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
        // 1. MOCK FILM DAO con Mockito (bypassa il costruttore e l'errore JNDI)
        final FilmDAO mockFilmDao = Mockito.mock(FilmDAO.class);

        // Mock per findAll()
        final List<FilmBean> list100 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final FilmBean f = new FilmBean();
            f.setIdFilm(i);
            f.setNome("Film " + i);
            list100.add(f);
        }
        Mockito.when(mockFilmDao.findAll()).thenReturn(list100);

        // Mock per findById() usando thenAnswer per usare l'ID passato come parametro
        Mockito.when(mockFilmDao.findById(anyInt())).thenAnswer(invocation -> {
            int id = invocation.getArgument(0);
            final FilmBean f = new FilmBean();
            f.setIdFilm(id);
            f.setNome("Film Trovato " + id);
            // Simula la restituzione di una valutazione per evitare NullPointerException in modifyFilm
            f.setValutazione(5); 
            return f;
        });

        // Mock per findByName()
        Mockito.when(mockFilmDao.findByName(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            final List<FilmBean> list = new ArrayList<>();
            final FilmBean f = new FilmBean();
            f.setIdFilm(1);
            f.setNome(name);
            list.add(f);
            return list;
        });

        // Mock per doRetrieveConsigliati()
        final List<FilmBean> listConsigliati = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final FilmBean f = new FilmBean();
            f.setIdFilm(i);
            listConsigliati.add(f);
        }
        Mockito.when(mockFilmDao.doRetrieveConsigliati(anyString())).thenReturn(listConsigliati);

        // I metodi void (save, update, delete) non fanno nulla di default con Mockito.


        // 2. MOCK FILMGENERE DAO con Mockito
        final FilmGenereDAO mockFilmGenereDao = Mockito.mock(FilmGenereDAO.class);
        Mockito.when(mockFilmGenereDao.findByIdFilm(anyInt())).thenAnswer(invocation -> {
            int id = invocation.getArgument(0);
            final List<FilmGenereBean> list = new ArrayList<>();
            list.add(new FilmGenereBean(id, "Azione"));
            return list;
        });


        // 3. MOCK GENERE DAO con Mockito
        final GenereDAO mockGenereDao = Mockito.mock(GenereDAO.class);
        final List<String> listGeneri = new ArrayList<>();
        listGeneri.add("Azione");
        listGeneri.add("Commedia");
        Mockito.when(mockGenereDao.findAllString()).thenReturn(listGeneri);


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