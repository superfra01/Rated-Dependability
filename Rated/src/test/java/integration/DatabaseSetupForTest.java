package integration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public class DatabaseSetupForTest {

    private static JdbcDataSource dataSource;
    private static boolean initialized;

    public static void configureH2() {
        try {
            // 1. Imposta le proprietà di sistema per dire a Java di usare il JNDI di Tomcat (catalina)
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

            // 2. Crea e configura il DataSource H2 (database in memoria)
            // Lo salviamo nel campo statico 'dataSource' per poterlo restituire con getH2DataSource()
            dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL");
            dataSource.setUser("sa");
            dataSource.setPassword("");

            // 3. Inizializza il contesto JNDI
            Context context = new InitialContext();

            // 4. Crea i sotto-contesti necessari (java:comp/env/jdbc)
            try {
                context.createSubcontext("java:");
            } catch (NamingException e) { /* Ignora se esiste già */ }
            try {
                context.createSubcontext("java:comp");
            } catch (NamingException e) { /* Ignora se esiste già */ }
            try {
                context.createSubcontext("java:comp/env");
            } catch (NamingException e) { /* Ignora se esiste già */ }
            try {
                context.createSubcontext("java:comp/env/jdbc");
            } catch (NamingException e) { /* Ignora se esiste già */ }

            // 5. Collega (bind) il DataSource H2 al nome che DriverManagerConnectionPool cerca
            context.rebind("java:comp/env/jdbc/RatedDB", dataSource);

            if (!initialized) {
                initializeSchema();
                initialized = true;
            }

        } catch (NamingException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la configurazione JNDI per i test", e);
        }
    }

    /**
     * Metodo reintrodotto per compatibilità con i test esistenti.
     * Restituisce il DataSource configurato.
     */
    public static DataSource getH2DataSource() {
        if (dataSource == null) {
            configureH2();
        }
        return dataSource;
    }

    private static void initializeSchema() {
        final Path initPath = resolveInitSqlPath();
        final List<String> statements = loadStatements(initPath);
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            for (final String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'inizializzazione dello schema di test", e);
        }
    }

    private static Path resolveInitSqlPath() {
        final Path localPath = Paths.get("init.sql");
        if (Files.exists(localPath)) {
            return localPath;
        }
        final Path repoRootPath = Paths.get("Rated", "init.sql");
        if (Files.exists(repoRootPath)) {
            return repoRootPath;
        }
        throw new IllegalStateException("init.sql non trovato per l'inizializzazione dei test.");
    }

    private static List<String> loadStatements(final Path initPath) {
        final List<String> statements = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        try {
            for (final String rawLine : Files.readAllLines(initPath)) {
                final String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                if (line.toUpperCase().startsWith("DROP DATABASE")
                        || line.toUpperCase().startsWith("CREATE DATABASE")
                        || line.toUpperCase().startsWith("USE ")) {
                    continue;
                }
                final String sanitized = line.replace("INSERT IGNORE INTO", "INSERT INTO");
                current.append(sanitized).append(' ');
                if (line.endsWith(";")) {
                    final String statement = current.toString().trim();
                    statements.add(statement.substring(0, statement.length() - 1));
                    current.setLength(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la lettura di init.sql", e);
        }
        return statements;
    }
}
