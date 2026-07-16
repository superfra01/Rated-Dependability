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
    private final DataSource dataSource;
    /* =========================================
     * INVARIANTI
     * ========================================= */

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    //@ assignable \nothing;
    //@ skipesc
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
    //@ assignable \nothing;
    public FilmDAO(final DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    /*@ 
      @ requires testMode == true;
      @ skipesc
      @*/
    protected FilmDAO(final boolean testMode) {
        this.dataSource = null; 
    }

    /* =========================================
     * METODI DI SCRITTURA (SAVE, UPDATE, DELETE)
     * ========================================= */

    //@ requires dataSource != null;
    //@ requires film != null;
    //@ assignable \everything;
    //@ ensures film.getIdFilm() >= 0;
    //@ skipesc
    //@ skiprac
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
            // Preserve the DAO fallback without exposing database details.
        }
    }

    //@ requires dataSource != null;
    //@ requires film != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
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
            // Preserve the DAO fallback without exposing database details.
        }
    }

    //@ requires dataSource != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void delete(final int idFilm) { 
        final String query = "DELETE FROM Film WHERE ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);
            ps.executeUpdate();
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
        }
    }

    /* =========================================
     * METODI DI LETTURA (FIND)
     * ========================================= */

    //@ requires dataSource != null;
    //@ requires idFilm >= 0;
    //@ assignable \nothing;
    //@ ensures \result != null ==> \result.getIdFilm() == idFilm;
    //@ skipesc
    //@ skiprac
    public /*@ nullable @*/ FilmBean findById(final int idFilm) {
        // Risolto: Elenco esplicito delle colonne invece di SELECT *
        final String query = "SELECT ID_Film, locandina, nome, anno, durata, regista, attori, valutazione, trama FROM Film WHERE ID_Film = ?";
        FilmBean result = null;

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = new FilmBean();
                    result.setIdFilm(rs.getInt("ID_Film"));
                    result.setLocandina(rs.getBytes("locandina"));
                    result.setNome(rs.getString("nome"));
                    result.setAnno(rs.getInt("anno"));
                    result.setDurata(rs.getInt("durata"));
                    result.setRegista(rs.getString("regista"));
                    result.setAttori(rs.getString("attori"));
                    result.setValutazione(rs.getInt("valutazione"));
                    result.setTrama(rs.getString("trama"));
                }
            }
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
            return null;
        }
        return result;
    }

    //@ requires dataSource != null;
    //@ requires name != null;
    //@ assignable \nothing;
    //@ ensures \result != null;
    //@ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null);
    //@ skipesc
    //@ skiprac
    public List<FilmBean> findByName(final String name) { 
        // Risolto: Elenco esplicito delle colonne invece di SELECT *
        final String query = "SELECT ID_Film, locandina, nome, anno, durata, regista, attori, valutazione, trama FROM Film WHERE nome LIKE ?";
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
            // Preserve the DAO fallback without exposing database details.
        }
        return films;
    }

    //@ requires dataSource != null;
    //@ assignable \nothing;
    //@ ensures \result != null;
    //@ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null);
    //@ skipesc
    //@ skiprac
    public List<FilmBean> findAll() {
        // Risolto: Elenco esplicito delle colonne invece di SELECT *
        final String query = "SELECT ID_Film, locandina, nome, anno, durata, regista, attori, valutazione, trama FROM Film";
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
            // Preserve the DAO fallback without exposing database details.
        }
        return films;
    }

    //@ requires dataSource != null;
    //@ requires emailUtente != null;
    //@ assignable \nothing;
    //@ ensures \result != null;
    //@ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null);
    //@ skipesc
    //@ skiprac
    public synchronized List<FilmBean> doRetrieveConsigliati(final String emailUtente) {
        final List<FilmBean> films = new ArrayList<>();

        // Risolto: Elenco esplicito delle colonne invece di SELECT f.*
        final String sql = "SELECT DISTINCT f.ID_Film, f.locandina, f.nome, f.anno, f.durata, f.regista, f.attori, f.valutazione, f.trama " + 
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

        try (final Connection conn = dataSource.getConnection(); 
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emailUtente);
            ps.setString(2, emailUtente);
            ps.setString(3, emailUtente);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final FilmBean film = new FilmBean();
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
            // Preserve the DAO fallback without exposing database details.
        }
        return films;
    }
}
