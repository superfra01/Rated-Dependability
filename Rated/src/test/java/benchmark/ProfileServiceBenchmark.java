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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        final UtenteDAO mockUtenteDao = new UtenteDAO() {
            @Override
            public UtenteBean findByEmail(String email) {
                UtenteBean u = new UtenteBean();
                u.setEmail(email);
                u.setUsername("User_" + email);
                return u;
            }
            @Override
            public UtenteBean findByUsername(String username) {
                UtenteBean u = new UtenteBean();
                u.setUsername(username);
                return u;
            }
            @Override public void update(UtenteBean u) {}
        };

        // 2. MOCK PREFERENZA DAO
        final PreferenzaDAO mockPreferenzaDao = new PreferenzaDAO() {
            @Override
            public List<PreferenzaBean> findByEmail(String email) {
                List<PreferenzaBean> list = new ArrayList<>();
                list.add(new PreferenzaBean(email, "Azione"));
                list.add(new PreferenzaBean(email, "Fantascienza"));
                return list;
            }
            @Override public void save(PreferenzaBean p) {}
            @Override public void deleteByEmail(String email) {}
        };

        // 3. MOCK INTERESSE DAO (Watchlist)
        final InteresseDAO mockInteresseDao = new InteresseDAO() {
            @Override
            public List<FilmBean> doRetrieveFilmsByUtente(String username) {
                List<FilmBean> list = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    list.add(new FilmBean());
                }
                return list;
            }
            @Override public void save(model.Entity.InteresseBean i) {}
            @Override public void delete(String email, int idFilm) {}
        };

        // 4. MOCK VISTO DAO
        final VistoDAO mockVistoDao = new VistoDAO() {
            @Override
            public List<FilmBean> doRetrieveFilmsByUtente(String username) {
                List<FilmBean> list = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    list.add(new FilmBean());
                }
                return list;
            }
            @Override public void save(model.Entity.VistoBean v) {}
            @Override public void delete(String email, int idFilm) {}
        };

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