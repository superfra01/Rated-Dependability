package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.mockito.Mockito;

import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class CatalogoServiceBenchmark {

    private CatalogoService catalogoService;

    private FilmDAO mockFilmDAO;
    private FilmGenereDAO mockFilmGenereDAO;
    private GenereDAO mockGenereDAO;

    // Variabili di test
    private final int testIdFilm = 1;
    private final String testNomeFilm = "Inception";
    private final String testAttori = "Leonardo DiCaprio, Joseph Gordon-Levitt";
    private final String testRegista = "Christopher Nolan";
    private final String testTrama = "Un ladro che ruba segreti corporativi attraverso l'uso della tecnologia di condivisione dei sogni...";
    private final int testAnno = 2010;
    private final int testDurata = 148;
    private final byte[] testLocandina = new byte[0];
    private final String[] testGeneri = {"Fantascienza", "Azione", "Thriller"};
    private final String testEmail = "utente@test.com";

    private FilmBean mockFilm;
    private UtenteBean mockUtente;
    private List<RecensioneBean> testRecensioni;

    @Setup(Level.Trial)
    public void setUp() {
        // 1. Inizializzazione mock in modalità stubOnly per evitare overhead
        mockFilmDAO = Mockito.mock(FilmDAO.class, Mockito.withSettings().stubOnly());
        mockFilmGenereDAO = Mockito.mock(FilmGenereDAO.class, Mockito.withSettings().stubOnly());
        mockGenereDAO = Mockito.mock(GenereDAO.class, Mockito.withSettings().stubOnly());

        // 2. Iniezione nel Service
        catalogoService = new CatalogoService(mockFilmDAO, mockFilmGenereDAO, mockGenereDAO);

        // 3. Preparazione dei dati di test per evitare NullPointerException
        mockFilm = new FilmBean();
        mockFilm.setIdFilm(testIdFilm);
        mockFilm.setNome(testNomeFilm);
        mockFilm.setValutazione(5); // Necessario per modifyFilm()

        List<FilmBean> mockFilmList = Arrays.asList(mockFilm); // Necessario per addFilm() che fa films.get(0)

        mockUtente = new UtenteBean();
        mockUtente.setEmail(testEmail);

        RecensioneBean recensione = new RecensioneBean();
        recensione.setIdFilm(testIdFilm);
        testRecensioni = Arrays.asList(recensione);

        List<FilmGenereBean> mockFilmGeneriList = Arrays.asList(
            new FilmGenereBean(testIdFilm, "Fantascienza"),
            new FilmGenereBean(testIdFilm, "Azione")
        );

        List<String> mockAllGeneriList = Arrays.asList("Azione", "Fantascienza", "Commedia", "Drammatico");

        // 4. Configurazione comportamenti dei mock
        Mockito.when(mockFilmDAO.findAll()).thenReturn(mockFilmList);
        Mockito.when(mockFilmDAO.findByName(testNomeFilm)).thenReturn(mockFilmList);
        Mockito.when(mockFilmDAO.findById(testIdFilm)).thenReturn(mockFilm);
        Mockito.when(mockFilmDAO.doRetrieveConsigliati(testEmail)).thenReturn(mockFilmList);
        
        Mockito.when(mockFilmGenereDAO.findByIdFilm(testIdFilm)).thenReturn(mockFilmGeneriList);
        Mockito.when(mockGenereDAO.findAllString()).thenReturn(mockAllGeneriList);
    }

    @Benchmark
    public void benchmarkGetFilms(Blackhole bh) {
        bh.consume(catalogoService.getFilms());
    }

    @Benchmark
    public void benchmarkAggiungiFilm(Blackhole bh) {
        catalogoService.aggiungiFilm(testNomeFilm, testAnno, testDurata, testGeneri, testRegista, testAttori, testLocandina, testTrama);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkRemoveFilmByBean(Blackhole bh) {
        catalogoService.removeFilmByBean(mockFilm);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkRicercaFilm(Blackhole bh) {
        bh.consume(catalogoService.ricercaFilm(testNomeFilm));
    }

    @Benchmark
    public void benchmarkGetFilmById(Blackhole bh) {
        bh.consume(catalogoService.getFilm(testIdFilm));
    }

    @Benchmark
    public void benchmarkGetFilmsByRecensioni(Blackhole bh) {
        bh.consume(catalogoService.getFilms(testRecensioni));
    }

    @Benchmark
    public void benchmarkAddFilm(Blackhole bh) {
        catalogoService.addFilm(testAnno, testAttori, testDurata, testGeneri, testLocandina, testNomeFilm, testRegista, testTrama);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkModifyFilm(Blackhole bh) {
        catalogoService.modifyFilm(testIdFilm, testAnno, testAttori, testDurata, testGeneri, testLocandina, testNomeFilm, testRegista, testTrama);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkRemoveFilmById(Blackhole bh) {
        catalogoService.removeFilm(testIdFilm);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkGetGeneri(Blackhole bh) {
        bh.consume(catalogoService.getGeneri(testIdFilm));
    }

    @Benchmark
    public void benchmarkGetAllGeneri(Blackhole bh) {
        bh.consume(catalogoService.getAllGeneri());
    }

    @Benchmark
    public void benchmarkGetFilmCompatibili(Blackhole bh) {
        bh.consume(catalogoService.getFilmCompatibili(mockUtente));
    }
}