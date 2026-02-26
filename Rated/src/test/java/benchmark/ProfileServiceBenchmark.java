package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.mockito.Mockito;

import model.DAO.UtenteDAO;
import model.DAO.PreferenzaDAO;
import model.DAO.InteresseDAO;
import model.DAO.VistoDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;
import model.Entity.PreferenzaBean;
import model.Entity.RecensioneBean;
import model.Entity.InteresseBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class ProfileServiceBenchmark {

    private ProfileService profileService;
    
    private UtenteDAO mockUtenteDAO;
    private PreferenzaDAO mockPreferenzaDAO;
    private InteresseDAO mockInteresseDAO;
    private VistoDAO mockVistoDAO;

    // Dati di test
    private final String testEmail = "test@email.com";
    private final String testUsername = "testuser";
    private final String newUsername = "newuser";
    private final String testPassword = "password123";
    private final String testBio = "Questa è una biografia di test";
    private final byte[] testIcon = new byte[0];
    private final String[] testGeneri = {"Azione", "Commedia", "Drammatico"};
    private final int testFilmId = 123;
    
    private List<RecensioneBean> testRecensioni;

    @Setup(Level.Trial)
    public void setUp() {
        // 1. Inizializza i mock in modalità "stubOnly" per evitare overhead e memory leak
        mockUtenteDAO = Mockito.mock(UtenteDAO.class, Mockito.withSettings().stubOnly());
        mockPreferenzaDAO = Mockito.mock(PreferenzaDAO.class, Mockito.withSettings().stubOnly());
        mockInteresseDAO = Mockito.mock(InteresseDAO.class, Mockito.withSettings().stubOnly());
        mockVistoDAO = Mockito.mock(VistoDAO.class, Mockito.withSettings().stubOnly());

        // 2. Inietta i mock tramite il costruttore del service
        profileService = new ProfileService(mockUtenteDAO, mockPreferenzaDAO, mockInteresseDAO, mockVistoDAO);

        // 3. Setup dei comportamenti di default per i mock
        
        // Mock UtenteDAO
        UtenteBean mockUser = new UtenteBean();
        mockUser.setEmail(testEmail);
        mockUser.setUsername(testUsername);
        
        Mockito.when(mockUtenteDAO.findByEmail(testEmail)).thenReturn(mockUser);
        Mockito.when(mockUtenteDAO.findByUsername(testUsername)).thenReturn(mockUser);
        Mockito.when(mockUtenteDAO.findByUsername(newUsername)).thenReturn(null); // Utile per testare ProfileUpdate
        
        // Mock PreferenzaDAO
        List<PreferenzaBean> mockPreferenze = new ArrayList<>();
        mockPreferenze.add(new PreferenzaBean(testEmail, "Azione"));
        mockPreferenze.add(new PreferenzaBean(testEmail, "Commedia"));
        Mockito.when(mockPreferenzaDAO.findByEmail(testEmail)).thenReturn(mockPreferenze);

        // Mock InteresseDAO
        InteresseBean mockInteresse = new InteresseBean();
        mockInteresse.setEmail(testEmail);
        mockInteresse.setIdFilm(testFilmId);
        mockInteresse.setInteresse(true);
        Mockito.when(mockInteresseDAO.findByEmailAndIdFilm(testEmail, testFilmId)).thenReturn(mockInteresse);

        // Setup dati per getUsers
        RecensioneBean recensione1 = new RecensioneBean();
        recensione1.setEmail(testEmail);
        testRecensioni = Arrays.asList(recensione1);
    }

    @Benchmark
    public void benchmarkFindByUsername(Blackhole bh) {
        bh.consume(profileService.findByUsername(testUsername));
    }

    @Benchmark
    public void benchmarkProfileUpdate(Blackhole bh) {
        // Attenzione: questo metodo invoca PasswordUtility.hashPassword, il cui tempo verrà incluso nel benchmark.
        bh.consume(profileService.ProfileUpdate(newUsername, testEmail, testPassword, testBio, testIcon));
    }

    @Benchmark
    public void benchmarkPasswordUpdate(Blackhole bh) {
        bh.consume(profileService.PasswordUpdate(testEmail, "nuovapassword"));
    }

    @Benchmark
    public void benchmarkGetPreferenze(Blackhole bh) {
        bh.consume(profileService.getPreferenze(testEmail));
    }

    @Benchmark
    public void benchmarkGetUsers(Blackhole bh) {
        bh.consume(profileService.getUsers(testRecensioni));
    }

    @Benchmark
    public void benchmarkAggiungiAllaWatchlist(Blackhole bh) {
        // Anche se è void, possiamo far consumare a JMH il completamento del metodo per evitare DCE (Dead Code Elimination)
        profileService.aggiungiAllaWatchlist(testEmail, testFilmId);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkAggiornaPreferenzeUtente(Blackhole bh) {
        profileService.aggiornaPreferenzeUtente(testEmail, testGeneri);
        bh.consume(true);
    }

    @Benchmark
    public void benchmarkIsFilmInWatchlist(Blackhole bh) {
        bh.consume(profileService.isFilmInWatchlist(testEmail, testFilmId));
    }
}