package benchmark;

import model.DAO.InteresseDAO;
import model.DAO.PreferenzaDAO;
import model.DAO.UtenteDAO;
import model.DAO.VistoDAO;
import model.Entity.FilmBean;
import model.Entity.PreferenzaBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class ProfileServiceBenchmark {

    private ProfileService service;
    private List<RecensioneBean> listaRecensioniTest;
    private String emailTest;
    private String[] generiTest;

    @Setup(Level.Trial)
    public void setup() {
        // 1. MOCK UTENTE DAO
        final UtenteDAO mockUtenteDao = Mockito.mock(UtenteDAO.class);
        
        Mockito.when(mockUtenteDao.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            UtenteBean u = new UtenteBean();
            u.setEmail(email);
            u.setUsername("User_" + email);
            return u;
        });
        
        Mockito.when(mockUtenteDao.findByUsername(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            UtenteBean u = new UtenteBean();
            u.setUsername(username);
            return u;
        });
        // Il metodo update restituisce void, quindi Mockito non farà nulla di default.

        // 2. MOCK PREFERENZA DAO
        final PreferenzaDAO mockPreferenzaDao = Mockito.mock(PreferenzaDAO.class);
        
        Mockito.when(mockPreferenzaDao.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            List<PreferenzaBean> list = new ArrayList<>();
            list.add(new PreferenzaBean(email, "Azione"));
            list.add(new PreferenzaBean(email, "Fantascienza"));
            return list;
        });

        // 3. MOCK INTERESSE DAO (Watchlist)
        final InteresseDAO mockInteresseDao = Mockito.mock(InteresseDAO.class);
        
        Mockito.when(mockInteresseDao.doRetrieveFilmsByUtente(anyString())).thenAnswer(invocation -> {
            List<FilmBean> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                list.add(new FilmBean());
            }
            return list;
        });

        // 4. MOCK VISTO DAO
        final VistoDAO mockVistoDao = Mockito.mock(VistoDAO.class);
        
        Mockito.when(mockVistoDao.doRetrieveFilmsByUtente(anyString())).thenAnswer(invocation -> {
            List<FilmBean> list = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                list.add(new FilmBean());
            }
            return list;
        });

        // 5. INIEZIONE NEL SERVICE
        this.service = new ProfileService(mockUtenteDao, mockPreferenzaDao, mockInteresseDao, mockVistoDao);

        // 6. PREPARAZIONE DATI PER I TEST
        this.listaRecensioniTest = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final RecensioneBean r = new RecensioneBean();
            r.setEmail("utente" + i + "@email.com");
            listaRecensioniTest.add(r);
        }
        
        this.emailTest = "mario.rossi@email.com";
        this.generiTest = new String[]{"Azione", "Horror", "Commedia", "Drammatico"};
    }

    @Benchmark
    public void testGetUsersByRecensioni(Blackhole bh) {
        final HashMap<String, String> result = service.getUsers(listaRecensioniTest);
        bh.consume(result);
    }

    @Benchmark
    public void testGetPreferenze(Blackhole bh) {
        final List<String> result = service.getPreferenze(emailTest);
        bh.consume(result);
    }

    @Benchmark
    public void testAggiornaPreferenzeUtente(Blackhole bh) {
        service.aggiornaPreferenzeUtente(emailTest, generiTest);
        bh.consume(true);
    }

    @Benchmark
    public void testRetrieveWatchedFilms(Blackhole bh) {
        final List<FilmBean> result = service.retrieveWatchedFilms("usernameTest");
        bh.consume(result);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(ProfileServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}