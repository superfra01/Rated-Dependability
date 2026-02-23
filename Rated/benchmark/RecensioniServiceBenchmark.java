package benchmark;

import model.DAO.FilmDAO;
import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.ValutazioneDAO;
import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.ValutazioneBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

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
public class RecensioniServiceBenchmark {

    private RecensioniService service;

    // Variabili per i test
    private String emailTest = "user@test.com";
    private int idFilmTest = 1;
    private List<RecensioneBean> listaRecensioniMock;

    @Setup(Level.Trial)
    public void setup() {
    	
        // --- 1. PREPARIAMO I DATI FINTI ---
        listaRecensioniMock = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final RecensioneBean r = new RecensioneBean();
            r.setIdFilm(idFilmTest);
            r.setValutazione((i % 5) + 1); // Voti da 1 a 5 a rotazione
            // Simuliamo che 1 recensione su 10 sia segnalata
            r.setNReports(i % 10 == 0 ? 5 : 0);
            listaRecensioniMock.add(r);
        }

        // --- 2. MOCK DI RECENSIONE DAO ---
        final RecensioneDAO mockRecensioneDAO = new RecensioneDAO(true) { 
            @Override
            public RecensioneBean findById(String email, int idFilm) {
                return null;
            }

            @Override
            public List<RecensioneBean> findByIdFilm(int idFilm) {
                return listaRecensioniMock;
            }

            @Override
            public List<RecensioneBean> findAll() {
                return listaRecensioniMock;
            }

            @Override public void save(RecensioneBean r) {}
            @Override public void update(RecensioneBean r) {}
        };

        // --- 3. MOCK DI FILM DAO ---
        final FilmDAO mockFilmDAO = new FilmDAO(true) {
            @Override
            public FilmBean findById(int idFilm) {
                final FilmBean f = new FilmBean();
                f.setIdFilm(idFilm);
                return f;
            }
            @Override public void update(FilmBean f) {} // Non fa nulla
        };

        // --- 4. MOCK DI ALTRI DAO ---
        final ValutazioneDAO mockValutazioneDAO = new ValutazioneDAO(true) {
            @Override public ValutazioneBean findById(String email, String emailRecensore, int idFilm) { return null; }
            @Override public void save(ValutazioneBean v) {}
        };

        final ReportDAO mockReportDAO = new ReportDAO(true) {
             // Metodi vuoti se necessario
        };

        // --- 5. INIEZIONE DELLE DIPENDENZE ---
        this.service = new RecensioniService(mockRecensioneDAO, mockValutazioneDAO, mockReportDAO, mockFilmDAO);
    }

    // --- BENCHMARK 1: Logica di Filtraggio ---
    @Benchmark
    public void testGetAllRecensioniSegnalate(Blackhole bh) {
        final List<RecensioneBean> result = service.GetAllRecensioniSegnalate();
        bh.consume(result);
    }

    // --- BENCHMARK 2: Aggiunta Recensione ---
    @Benchmark
    public void testAddRecensione(Blackhole bh) {
        service.addRecensione(emailTest, idFilmTest, "Bella trama", "Titolo", 5);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(RecensioniServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}