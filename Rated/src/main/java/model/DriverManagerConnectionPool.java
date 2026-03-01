package model;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DriverManagerConnectionPool {

    // Riferimento al pool di connessioni gestito dal server web
    private static volatile DataSource dataSource;

    // Costruttore privato per impedire l'istanziamento della classe
    private DriverManagerConnectionPool() {}

    // Metodo per recuperare il DataSource tramite pattern Singleton
    private static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DriverManagerConnectionPool.class) {
                if (dataSource == null) {
                    try {
                        Context initCtx = new InitialContext();
                        Context envCtx = (Context) initCtx.lookup("java:comp/env");
                        
                        // Cerca la risorsa configurata nel file context.xml
                        dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
                    } catch (NamingException e) {
                        throw new RuntimeException("Errore durante la configurazione del DataSource JNDI", e);
                    }
                }
            }
        }
        return dataSource;
    }

    /**
     * Fornisce una connessione prelevandola dal pool di Tomcat.
     * Risolve lo smell chiudendo la connessione se la configurazione fallisce.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = getDataSource().getConnection();
        try {
            // Manteniamo il false di default come richiesto
            connection.setAutoCommit(false); 
            return connection;
        } catch (SQLException e) {
            // Se setAutoCommit fallisce, la connessione non verrebbe mai chiusa dal chiamante.
            // La chiudiamo qui per prevenire il leak prima di rilanciare l'eccezione.
            if (connection != null) {
                connection.close();
            }
            throw e;
        }
    }

    /**
     * Rilascia la connessione restituendola al pool.
     */
    public static void releaseConnection(final Connection connection) {
        if (connection != null) {
            try {
                connection.close(); // Restituisce la connessione al pool
            } catch (SQLException e) {
                System.err.println("Errore durante il rilascio della connessione: " + e.getMessage());
            }
        }
    }
}