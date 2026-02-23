package benchmark;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.AutenticationService;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import utilities.PasswordUtility; 

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 15, time = 1)
@Measurement(iterations = 40, time = 1)
public class AutenticationServiceBenchmark {

    private AutenticationService service;
    
    // Dati di test
    private final String VALID_EMAIL = "user@test.com";
    private final String VALID_PASSWORD = "PasswordSegreta123!";
    private final String WRONG_PASSWORD = "Sbagliata";
    private final String NEW_USER_EMAIL = "new@test.com";
    private final String NEW_USER_NAME = "NewUser";

    private HttpSession mockSession;

    @Setup(Level.Trial)
    public void setup() {
        // --- 1. MOCK UTENTE DAO ---
        final UtenteDAO mockDao = new UtenteDAO(true) { 
            @Override
            public UtenteBean findByEmail(String email) {
                if ("user@test.com".equals(email)) { 
                    final UtenteBean u = new UtenteBean();
                    u.setEmail(email);
                    u.setPassword(PasswordUtility.hashPassword("PasswordSegreta123!")); 
                    return u;
                }
                return null; 
            }

            @Override
            public UtenteBean findByUsername(String username) {
                if ("ExistingUser".equals(username)) {
                    return new UtenteBean();
                }
                return null; 
            }

            @Override
            public void save(UtenteBean u) {
                // Simulazione salvataggio veloce
            }
        };

        // --- 2. MOCK SESSION ---
        this.mockSession = new javax.servlet.http.HttpSession() {
            @Override
            public void invalidate() {
            }

            @Override public long getCreationTime() { return 0; }
            @Override public String getId() { return "mockId"; }
            @Override public long getLastAccessedTime() { return 0; }
            @Override public javax.servlet.ServletContext getServletContext() { return null; }
            @Override public void setMaxInactiveInterval(int interval) {}
            @Override public int getMaxInactiveInterval() { return 0; }
            @Override public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }
            @Override public Object getAttribute(String name) { return null; }
            @Override public Object getValue(String name) { return null; }
            @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
            @Override public String[] getValueNames() { return new String[0]; }
            @Override public void setAttribute(String name, Object value) {}
            @Override public void putValue(String name, Object value) {}
            @Override public void removeAttribute(String name) {}
            @Override public void removeValue(String name) {}
            
            @Override 
            public boolean isNew() { 
                return false; 
            }
        };

        // --- 3. INIEZIONE ---
        this.service = new AutenticationService(mockDao);
    }

    @Benchmark
    public void testLoginSuccess(Blackhole bh) {
        final UtenteBean u = service.login(VALID_EMAIL, VALID_PASSWORD);
        bh.consume(u);
    }

    @Benchmark
    public void testLoginWrongPassword(Blackhole bh) {
        final UtenteBean u = service.login(VALID_EMAIL, WRONG_PASSWORD);
        bh.consume(u);
    }
    
    @Benchmark
    public void testLoginUserNotFound(Blackhole bh) {
        final UtenteBean u = service.login("non@esiste.com", "qualcosa");
        bh.consume(u);
    }

    @Benchmark
    public void testRegister(Blackhole bh) {
        final UtenteBean u = service.register(NEW_USER_NAME, NEW_USER_EMAIL, VALID_PASSWORD, "Bio", null);
        bh.consume(u);
    }

    @Benchmark
    public void testLogout() {
        service.logout(mockSession);
    }

    public static void main(String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(AutenticationServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}