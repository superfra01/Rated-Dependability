package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import model.DriverManagerConnectionPool;

public class DriverManagerConnectionPoolTest {

    private MockedStatic<DriverManager> mockedDriverManager;
    private Connection mockConnection;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        // 1. Creiamo un finto oggetto Connection
        mockConnection = mock(Connection.class);

        // 2. Intercettiamo le chiamate statiche a DriverManager
        // Così, quando la classe farà DriverManager.getConnection(...), restituirà il nostro mock
        mockedDriverManager = mockStatic(DriverManager.class);
        mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                           .thenReturn(mockConnection);

        // 3. FIX TEST POLLUTION: Puliamo la lista statica 'freeDbConnections' usando la Reflection
        // Questo garantisce che ogni test parta con un pool vuoto.
        Field poolField = DriverManagerConnectionPool.class.getDeclaredField("freeDbConnections");
        poolField.setAccessible(true);
        List<Connection> pool = (List<Connection>) poolField.get(null);
        pool.clear();
    }

    @AfterEach
    public void tearDown() {
        // È FONDAMENTALE chiudere il mock statico alla fine di ogni test
        mockedDriverManager.close();
    }

    @Test
    public void testGetConnection_PoolVuoto_CreaNuovaConnessione() throws SQLException {
        // --- ACT ---
        Connection conn = DriverManagerConnectionPool.getConnection();

        // --- ASSERT ---
        assertNotNull(conn);
        assertEquals(mockConnection, conn, "Deve restituire il mock della connessione");
        
        // Verifica che setAutoCommit(false) sia stato chiamato sulla nuova connessione
        verify(mockConnection).setAutoCommit(false);
        
        // Verifica che il DriverManager sia stato chiamato esattamente 1 volta
        mockedDriverManager.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()), times(1));
    }

    @Test
    public void testReleaseConnection_AggiungeAlPool() throws Exception {
        // --- ARRANGE ---
        Connection primaConnessione = DriverManagerConnectionPool.getConnection(); // La estrae
        
        // --- ACT ---
        DriverManagerConnectionPool.releaseConnection(primaConnessione); // La rimette nel pool
        
        // --- ASSERT ---
        // Per verificare che sia nel pool, usiamo la Reflection per sbirciare nella lista privata
        Field poolField = DriverManagerConnectionPool.class.getDeclaredField("freeDbConnections");
        poolField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Connection> pool = (List<Connection>) poolField.get(null);
        
        assertEquals(1, pool.size(), "Il pool deve contenere esattamente 1 connessione");
        assertEquals(primaConnessione, pool.get(0));
    }

    @Test
    public void testGetConnection_PoolPieno_RiutilizzaConnessione() throws SQLException {
        // --- ARRANGE ---
        // Otteniamo una connessione e la rilasciamo (ora il pool ha 1 elemento)
        Connection conn1 = DriverManagerConnectionPool.getConnection();
        DriverManagerConnectionPool.releaseConnection(conn1);
        
        // Simuliamo che la connessione sia ancora aperta e funzionante
        when(mockConnection.isClosed()).thenReturn(false);

        // --- ACT ---
        Connection conn2 = DriverManagerConnectionPool.getConnection();

        // --- ASSERT ---
        assertEquals(conn1, conn2, "Deve riutilizzare la stessa identica connessione");
        
        // Il DriverManager deve essere stato invocato SOLO la prima volta (quando il pool era vuoto)
        mockedDriverManager.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()), times(1));
    }

    @Test
    public void testGetConnection_ConnessioneChiusa_CreaNuova() throws SQLException {
        // --- ARRANGE ---
        // Otteniamo una connessione e la rilasciamo
        Connection conn1 = DriverManagerConnectionPool.getConnection();
        DriverManagerConnectionPool.releaseConnection(conn1);
        
        // Simuliamo che nel frattempo la connessione sia morta/stata chiusa
        when(mockConnection.isClosed()).thenReturn(true);

        // --- ACT ---
        Connection conn2 = DriverManagerConnectionPool.getConnection();

        // --- ASSERT ---
        // Poiché era chiusa, il pool dovrebbe averne chiesta una NUOVA al DriverManager
        // Quindi il DriverManager deve essere stato chiamato in totale 2 volte.
        mockedDriverManager.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()), times(2));
    }
}