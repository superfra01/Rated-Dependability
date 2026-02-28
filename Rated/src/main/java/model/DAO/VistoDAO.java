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
import model.Entity.VistoBean;

public class VistoDAO {

    //@ spec_public
    private final DataSource dataSource; // Risolto: ora è final

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant dataSource != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    public VistoDAO() {
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
    public VistoDAO(final DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */

    //@ requires visto != null;
    //@ assignable \everything;
    public void save(final VistoBean visto) {
        final String selectQuery = "SELECT 1 FROM Visto WHERE email = ? AND ID_Film = ?";
        final String insertQuery = "INSERT INTO Visto (email, ID_Film) VALUES (?, ?)";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement selectPs = connection.prepareStatement(selectQuery);
             final PreparedStatement insertPs = connection.prepareStatement(insertQuery)) {

            selectPs.setString(1, visto.getEmail());
            selectPs.setInt(2, visto.getIdFilm());

            try (final ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) {
                    insertPs.setString(1, visto.getEmail());
                    insertPs.setInt(2, visto.getIdFilm());
                    insertPs.executeUpdate();
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    //@ requires email != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    //@ ensures \result != null ==> (\result.getEmail().equals(email) && \result.getIdFilm() == idFilm);
    public VistoBean findByEmailAndIdFilm(final String email, final int idFilm) {
        final String query = "SELECT * FROM Visto WHERE email = ? AND ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setInt(2, idFilm);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final VistoBean visto = new VistoBean(); // Risolto: bean final
                    visto.setEmail(rs.getString("email"));
                    visto.setIdFilm(rs.getInt("ID_Film"));
                    return visto;
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    //@ requires email != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public void delete(final String email, final int idFilm) {
        final String query = "DELETE FROM Visto WHERE email = ? AND ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setInt(2, idFilm);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    //@ requires username != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<FilmBean> doRetrieveFilmsByUtente(final String username) { // Risolto: parametro final
        final List<FilmBean> films = new ArrayList<>(); // Risolto: variabile locale final
        
        final String query = "SELECT f.ID_Film, f.Nome, f.Anno, f.Durata, f.Regista, f.Trama, f.Valutazione, f.Attori, f.Locandina " +
                       "FROM Film f " +
                       "JOIN Visto v ON f.ID_Film = v.ID_Film " +
                       "JOIN Utente_Registrato u ON v.email = u.email " +
                       "WHERE u.username = ?";

        try (final Connection con = dataSource.getConnection(); // Risolto: risorse final
             final PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final FilmBean film = new FilmBean(); // Risolto: bean final nel loop
                    
                    film.setIdFilm(rs.getInt("ID_Film"));
                    film.setNome(rs.getString("Nome"));
                    film.setAnno(rs.getInt("Anno"));
                    film.setDurata(rs.getInt("Durata"));
                    film.setRegista(rs.getString("Regista"));
                    film.setTrama(rs.getString("Trama"));
                    film.setValutazione(rs.getInt("Valutazione"));
                    film.setAttori(rs.getString("Attori"));
                    film.setLocandina(rs.getBytes("Locandina"));

                    films.add(film);
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return films;
    }
}