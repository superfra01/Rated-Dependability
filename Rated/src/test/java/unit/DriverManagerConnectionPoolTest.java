package unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.DriverManagerConnectionPool;

public class DriverManagerConnectionPoolTest {

    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Creiamo i mock per Connection e DataSource
        mockConnection = mock(Connection.class);
        mockDataSource = mock(DataSource.class);

        // 2. Quando il DataSource viene interrogato, restituisce la connessione fittizia
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        // 3. FIX TEST POLLUTION: Usiamo la Reflection per inserire il DataSource mockato 
        // nella variabile privata statica 'dataSource' della nostra classe
        Field dataSourceField = DriverManagerConnectionPool.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(null, mockDataSource);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Puliamo la variabile statica alla fine di ogni test per mantenere l'isolamento
        Field dataSourceField = DriverManagerConnectionPool.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(null, null);
    }

    @Test
    public void testGetConnection_RestituisceConnessioneDaDataSource() throws SQLException {
        // --- ACT ---
        Connection conn = DriverManagerConnectionPool.getConnection();

        // --- ASSERT ---
        assertNotNull(conn);
        assertEquals(mockConnection, conn, "Deve restituire il mock della connessione");
        
        // Verifica che getConnection() sia stato chiamato esattamente 1 volta sul DataSource
        verify(mockDataSource, times(1)).getConnection();
        
        // Verifica che setAutoCommit(false) sia stato chiamato sulla connessione estratta
        verify(mockConnection, times(1)).setAutoCommit(false);
    }

    @Test
    public void testReleaseConnection_ChiudeLaConnessione() throws Exception {
        // --- ACT ---
        DriverManagerConnectionPool.releaseConnection(mockConnection);
        
        // --- ASSERT ---
        // Con i DataSource gestiti (come Tomcat DBCP), chiamare .close() NON chiude
        // la connessione al database, ma la "rilascia" rimettendola nel pool.
        verify(mockConnection, times(1)).close();
    }

    @Test
    public void testReleaseConnection_ConnessioneNull_NonLanciaEccezioni() {
        // --- ACT & ASSERT ---
        // Verifica che passare null al metodo releaseConnection non causi un NullPointerException
        assertDoesNotThrow(() -> {
            DriverManagerConnectionPool.releaseConnection(null);
        });
    }
}