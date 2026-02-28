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

public class FilmDAO {

    //@ spec_public
    private final DataSource dataSource; // Risolto: ora è final

    /* =========================================
     * INVARIANTI
     * ========================================= */
    // Il dataSource non deve essere nullo per permettere le operazioni
    //@ public invariant dataSource != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    public FilmDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }

    //@ requires testDataSource != null;
    //@ ensures this.dataSource == testDataSource;
    public FilmDAO(final DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    // Costruttore protetto per test
    /*@ 
      @ requires testMode == true;
      @ skipesc
      @*/
    protected FilmDAO(final boolean testMode) {
        this.dataSource = null; // Necessario per variabili final
    }

    /* =========================================
     * METODI DI SCRITTURA (SAVE, UPDATE, DELETE)
     * ========================================= */

    //@ requires film != null;
    //@ assignable \everything;
    //@ ensures film.getIdFilm() >= 0;
    public void save(final FilmBean film) {
        final String query = "INSERT INTO Film (locandina, nome, anno, durata, regista, attori, valutazione, trama) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setBytes(1, film.getLocandina());
            ps.setString(2, film.getNome());
            ps.setInt(3, film.getAnno());
            ps.setInt(4, film.getDurata());
            ps.setString(5, film.getRegista());
            ps.setString(6, film.getAttori());
            ps.setInt(7, film.getValutazione());
            ps.setString(8, film.getTrama());

            ps.executeUpdate();

            try (final ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    film.setIdFilm(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creazione film fallita, nessun ID ottenuto.");
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    //@ requires film != null;
    //@ assignable \everything;
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

    //@ requires idFilm >= 0;
    //@ assignable \everything;
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

    /* =========================================
     * METODI DI LETTURA (FIND)
     * ========================================= */

    //@ requires idFilm >= 0;
    //@ assignable \everything;
    //@ ensures \result != null ==> \result.getIdFilm() == idFilm;
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

    //@ requires name != null;
    //@ assignable \everything;
    //@ ensures \result != null;
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

    //@ assignable \everything;
    //@ ensures \result != null;
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

    //@ requires emailUtente != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public synchronized List<FilmBean> doRetrieveConsigliati(final String emailUtente) { // Parametro final
        final List<FilmBean> films = new ArrayList<>(); // Variabile locale final

        final String sql = "SELECT DISTINCT f.* " + // Stringa SQL final
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

        try (final Connection conn = dataSource.getConnection(); // Risorse final
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emailUtente);
            ps.setString(2, emailUtente);
            ps.setString(3, emailUtente);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final FilmBean film = new FilmBean(); // Bean final nel loop
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
        } catch (final SQLException e) {
            e.printStackTrace(); 
        }
        return films;
    }
}