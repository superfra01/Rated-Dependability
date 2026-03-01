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
                        throw new RuntimeException("Errore durnate la configurazione del DataSource JNDI", e);
                    }
                }
            }
        }
        return dataSource;
    }

    /**
     * Fornisce una connessione prelevandola dal pool di Tomcat.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = getDataSource().getConnection();
        
        // Manteniamo il false di default come facevi nel tuo codice originale
        connection.setAutoCommit(false); 
        
        return connection;
    }

    /**
     * Rilascia la connessione.
     * ATTENZIONE: Con i DataSource, il metodo .close() NON chiude fisicamente 
     * la connessione con il DB, ma la restituisce semplicemente al pool!
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