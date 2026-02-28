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
import model.Entity.InteresseBean;

public class InteresseDAO {

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
    public InteresseDAO() {
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
    public InteresseDAO(final DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */

    //@ requires interesseBean != null;
    //@ assignable \everything;
    public void save(final InteresseBean interesseBean) {
        final String selectQuery = "SELECT 1 FROM Interesse WHERE email = ? AND ID_Film = ?";
        final String insertQuery = "INSERT INTO Interesse (email, ID_Film, interesse) VALUES (?, ?, ?)";
        final String updateQuery = "UPDATE Interesse SET interesse = ? WHERE email = ? AND ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement selectPs = connection.prepareStatement(selectQuery);
             final PreparedStatement insertPs = connection.prepareStatement(insertQuery);
             final PreparedStatement updatePs = connection.prepareStatement(updateQuery)) {

            selectPs.setString(1, interesseBean.getEmail());
            selectPs.setInt(2, interesseBean.getIdFilm());

            try (final ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    updatePs.setBoolean(1, interesseBean.isInteresse());
                    updatePs.setString(2, interesseBean.getEmail());
                    updatePs.setInt(3, interesseBean.getIdFilm());
                    updatePs.executeUpdate();
                } else {
                    insertPs.setString(1, interesseBean.getEmail());
                    insertPs.setInt(2, interesseBean.getIdFilm());
                    insertPs.setBoolean(3, interesseBean.isInteresse());
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
    public InteresseBean findByEmailAndIdFilm(final String email, final int idFilm) {
        final String query = "SELECT * FROM Interesse WHERE email = ? AND ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setInt(2, idFilm);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final InteresseBean interesse = new InteresseBean(); // Risolto: final
                    interesse.setEmail(rs.getString("email"));
                    interesse.setIdFilm(rs.getInt("ID_Film"));
                    interesse.setInteresse(rs.getBoolean("interesse"));
                    return interesse;
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
        final String query = "DELETE FROM Interesse WHERE email = ? AND ID_Film = ?";

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
                       "JOIN Interesse i ON f.ID_Film = i.ID_Film " +
                       "JOIN Utente_Registrato u ON i.email = u.email " +
                       "WHERE u.username = ? AND i.interesse = true"; 

        try (final Connection con = dataSource.getConnection(); // Risolto: risorse final
             final PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final FilmBean film = new FilmBean(); // Risolto: bean final
                    
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