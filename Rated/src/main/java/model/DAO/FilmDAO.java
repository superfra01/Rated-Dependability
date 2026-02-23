package model.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import model.Entity.FilmBean;
import model.Entity.UtenteBean;

public class FilmDAO {

    private DataSource dataSource; 

    public FilmDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }

    public FilmDAO(final DataSource testDataSource) { // Parametro final
        dataSource = testDataSource;
    }

    protected FilmDAO(final boolean testMode) { // Parametro final
        // Vuoto: non fa nulla
    }

    public void save(final FilmBean film) {
    // Rimuoviamo ID_Film dalla insert, ci pensa il database a generarlo
    final String query = "INSERT INTO Film (locandina, nome, anno, durata, regista, attori, valutazione, trama) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (final Connection connection = dataSource.getConnection();
         // Aggiungiamo il flag RETURN_GENERATED_KEYS
         final PreparedStatement ps = connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)) {

        // Nota: gli indici scalano di 1 perché abbiamo tolto l'ID dalla query
        ps.setBytes(1, film.getLocandina());
        ps.setString(2, film.getNome());
        ps.setInt(3, film.getAnno());
        ps.setInt(4, film.getDurata());
        ps.setString(5, film.getRegista());
        ps.setString(6, film.getAttori());
        ps.setInt(7, film.getValutazione());
        ps.setString(8, film.getTrama());

        ps.executeUpdate();

        // RECUPERO DELL'ID GENERATO
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                // Aggiorniamo il bean con il vero ID creato dal DB
                film.setIdFilm(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creazione film fallita, nessun ID ottenuto.");
            }
        }

    } catch (final SQLException e) {
        e.printStackTrace();
    }
}

    public FilmBean findById(final int idFilm) { 
        final String query = "SELECT * FROM Film WHERE ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final FilmBean film = new FilmBean();
                    film.setIdFilm(rs.getInt("ID_Film"));
                    film.setLocandina(rs.getBytes("locandina"));
                    film.setNome(rs.getString("nome"));
                    film.setAnno(rs.getInt("anno"));
                    film.setDurata(rs.getInt("durata"));
                    film.setRegista(rs.getString("regista"));
                    film.setAttori(rs.getString("attori"));
                    film.setValutazione(rs.getInt("valutazione"));
                    film.setTrama(rs.getString("trama"));
                    return film;
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<FilmBean> findByName(final String name) { 
        final String query = "SELECT * FROM Film WHERE nome LIKE ?";
        final List<FilmBean> films = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, name + "%");

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final FilmBean film = new FilmBean();
                    film.setIdFilm(rs.getInt("ID_Film"));
                    film.setLocandina(rs.getBytes("locandina"));
                    film.setNome(rs.getString("nome"));
                    film.setAnno(rs.getInt("anno"));
                    film.setDurata(rs.getInt("durata"));
                    film.setRegista(rs.getString("regista"));
                    film.setAttori(rs.getString("attori"));
                    film.setValutazione(rs.getInt("valutazione"));
                    film.setTrama(rs.getString("trama"));
                    films.add(film);
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return films;
    }

    public List<FilmBean> findAll() {
        final String query = "SELECT * FROM Film";
        final List<FilmBean> films = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final FilmBean film = new FilmBean();
                film.setIdFilm(rs.getInt("ID_Film"));
                film.setLocandina(rs.getBytes("locandina"));
                film.setNome(rs.getString("nome"));
                film.setAnno(rs.getInt("anno"));
                film.setDurata(rs.getInt("durata"));
                film.setRegista(rs.getString("regista"));
                film.setAttori(rs.getString("attori"));
                film.setValutazione(rs.getInt("valutazione"));
                film.setTrama(rs.getString("trama"));
                films.add(film);
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return films;
    }

    public void update(final FilmBean film) { 
        final String query = "UPDATE Film SET locandina = ?, nome = ?, anno = ?, durata = ?, regista = ?, attori = ?, valutazione = ?, trama = ? "
                           + "WHERE ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setBytes(1, film.getLocandina());
            ps.setString(2, film.getNome());
            ps.setInt(3, film.getAnno());
            ps.setInt(4, film.getDurata());
            ps.setString(5, film.getRegista());
            ps.setString(6, film.getAttori());
            ps.setInt(7, film.getValutazione());
            ps.setString(8, film.getTrama());
            ps.setInt(9, film.getIdFilm());

            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(final int idFilm) { 
        final String query = "DELETE FROM Film WHERE ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    
    public synchronized List<FilmBean> doRetrieveConsigliati(String emailUtente) {
        List<FilmBean> films = new ArrayList<>();

        /* * LOGICA SQL AGGIORNATA:
         * 1. JOIN con Preferenza: Prende solo film dei generi che piacciono all'utente.
         * 2. NOT IN Visto: Esclude i film già visti dall'utente.
         * 3. NOT IN Interesse (false): Esclude i film segnati come "non mi interessa".
         */
        String sql = "SELECT DISTINCT f.* " +
                     "FROM Film f " +
                     "JOIN Film_Genere fg ON f.ID_Film = fg.ID_Film " +
                     "JOIN Preferenza p ON fg.Nome_Genere = p.Nome_Genere " +
                     "WHERE p.email = ? " +
                     "AND f.ID_Film NOT IN ( " +
                     "    SELECT ID_Film FROM Visto WHERE email = ? " +
                     ") " +
                     "AND f.ID_Film NOT IN ( " +
                     "    SELECT ID_Film FROM Interesse WHERE email = ? AND interesse = false " +
                     ") " +
                     "ORDER BY f.Valutazione DESC";

        // Uso try-with-resources per chiudere automaticamente Connection, PS e RS
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Impostiamo i parametri (l'email serve 3 volte ora)
            ps.setString(1, emailUtente); // Per la tabella Preferenza
            ps.setString(2, emailUtente); // Per la tabella Visto (Esclusione)
            ps.setString(3, emailUtente); // Per la tabella Interesse (Esclusione)

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FilmBean film = new FilmBean();
                    
                    film.setIdFilm(rs.getInt("ID_Film"));
                    film.setLocandina(rs.getBytes("Locandina"));
                    film.setNome(rs.getString("Nome"));
                    film.setAnno(rs.getInt("Anno"));
                    film.setDurata(rs.getInt("Durata"));
                    film.setRegista(rs.getString("Regista"));
                    film.setAttori(rs.getString("Attori"));
                    film.setValutazione(rs.getInt("Valutazione"));
                    film.setTrama(rs.getString("Trama"));
                    
                    films.add(film);
                }
            }

        } catch (SQLException e) {
            // Loggare l'errore è meglio che stampare solo lo stack trace in produzione
            e.printStackTrace(); 
        }
        
        return films;
    }
}
