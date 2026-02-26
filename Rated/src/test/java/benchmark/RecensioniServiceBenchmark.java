package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.mockito.Mockito;

import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.FilmDAO;
import model.DAO.ValutazioneDAO;
import model.Entity.RecensioneBean;
import model.Entity.FilmBean;
import model.Entity.ValutazioneBean;
import sottosistemi.Gestione_Recensioni.service.RecensioniService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class RecensioniServiceBenchmark {

    private RecensioniService recensioniService;

    private RecensioneDAO mockRecensioneDAO;
    private ValutazioneDAO mockValutazioneDAO;
    private ReportDAO mockReportDAO;
    private FilmDAO mockFilmDAO;

    // Dati di test
    private final String testEmail = "utente@email.com";
    private final String testEmailRecensore = "recensore@email.com";
    private final int testFilmId = 1;
    private final String testTitolo = "Ottimo film";
    private final String testContenuto = "Mi è piaciuto tantissimo, lo consiglio.";
    private final int testValutazione = 5;

    @Setup(Level.Trial)
    public void setUp() {
        // 1. Inizializzazione dei mock in modalità stubOnly
        mockRecensioneDAO = Mockito.mock(RecensioneDAO.class, Mockito.withSettings().stubOnly());
        mockValutazioneDAO = Mockito.mock(ValutazioneDAO.class, Mockito.withSettings().stubOnly());
        mockReportDAO = Mockito.mock(ReportDAO.class, Mockito.withSettings().stubOnly());
        mockFilmDAO = Mockito.mock(FilmDAO.class, Mockito.withSettings().stubOnly());

        // 2. Iniezione dei mock nel Service
        recensioniService = new RecensioniService(mockRecensioneDAO, mockValutazioneDAO, mockReportDAO, mockFilmDAO);

        // 3. Preparazione dei bean fittizi
        RecensioneBean mockRecensione = new RecensioneBean();
        mockRecensione.setEmail(testEmailRecensore);
        mockRecensione.setIdFilm(testFilmId);
        mockRecensione.setNLike(10);
        mockRecensione.setNDislike(2);
        mockRecensione.setNReports(1);
        mockRecensione.setValutazione(4);

        FilmBean mockFilm = new FilmBean();
        mockFilm.setIdFilm(testFilmId);
        mockFilm.setValutazione(3);

        List<RecensioneBean> mockRecensioniList = Arrays.asList(mockRecensione);

        HashMap<String, ValutazioneBean> mockValutazioniMap = new HashMap<>();
        ValutazioneBean valBean = new ValutazioneBean();
        valBean.setLikeDislike(true);
        mockValutazioniMap.put(testEmail, valBean);

        // 4. Configurazione dei comportamenti di default (Happy Path per i benchmark)
        
        // Setup per addValutazione e report
        Mockito.when(mockRecensioneDAO.findById(testEmailRecensore, testFilmId)).thenReturn(mockRecensione);
        Mockito.when(mockValutazioneDAO.findById(testEmail, testEmailRecensore, testFilmId)).thenReturn(null); // Simula nuova valutazione
        
        // Setup per addRecensione e deleteRecensione
        Mockito.when(mockRecensioneDAO.findById(testEmail, testFilmId)).thenReturn(null); // Nessuna recensione esistente (per l'inserimento)
        Mockito.when(mockFilmDAO.findById(testFilmId)).thenReturn(mockFilm);
        Mockito.when(mockRecensioneDAO.findByIdFilm(testFilmId)).thenReturn(mockRecensioniList);

        // Setup per FindRecensioni e GetAllRecensioniSegnalate
        Mockito.when(mockRecensioneDAO.findByUser(testEmail)).thenReturn(mockRecensioniList);
        Mockito.when(mockRecensioneDAO.findAll()).thenReturn(mockRecensioniList);

        // Setup per GetValutazioni
        Mockito.when(mockValutazioneDAO.findByIdFilmAndEmail(testFilmId, testEmail)).thenReturn(mockValutazioniMap);
        
        // Setup per report
        Mockito.when(mockReportDAO.findById(testEmail, testEmailRecensore, testFilmId)).thenReturn(null);
    }

    @Benchmark
    public void benchmarkAddValutazione(Blackhole bh) {
        recensioniService.addValutazione(testEmail, testFilmId, testEmailRecensore, true);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkAddRecensione(Blackhole bh) {
        recensioniService.addRecensione(testEmail, testFilmId, testContenuto, testTitolo, testValutazione);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkFindRecensioni(Blackhole bh) {
        bh.consume(recensioniService.FindRecensioni(testEmail));
    }

    @Benchmark
    public void benchmarkDeleteRecensione(Blackhole bh) {
        recensioniService.deleteRecensione(testEmail, testFilmId);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkDeleteReports(Blackhole bh) {
        // Ritorna void, consumiamo un boolean fittizio per evitare ottimizzazioni estreme del JIT
        recensioniService.deleteReports(testEmail, testFilmId);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkGetRecensioni(Blackhole bh) {
        bh.consume(recensioniService.GetRecensioni(testFilmId));
    }

    @Benchmark
    public void benchmarkGetValutazioni(Blackhole bh) {
        bh.consume(recensioniService.GetValutazioni(testFilmId, testEmail));
    }

    @Benchmark
    public void benchmarkGetAllRecensioniSegnalate(Blackhole bh) {
        bh.consume(recensioniService.GetAllRecensioniSegnalate());
    }

    @Benchmark
    public void benchmarkReport(Blackhole bh) {
        recensioniService.report(testEmail, testEmailRecensore, testFilmId);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkGetRecensione(Blackhole bh) {
        // NOTA: Se non hai corretto il service per usare il DAO iniettato, 
        // questo metodo istanzierà un vero RecensioneDAO, rallentando enormemente 
        // il benchmark o fallendo se manca il DB.
        try {
            bh.consume(recensioniService.getRecensione(testFilmId, testEmail));
        } catch (Exception e) {
            bh.consume(e);
        }
    }
}