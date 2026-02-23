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

import model.Entity.FilmGenereBean;

public class FilmGenereDAO {

    private DataSource dataSource;

    public FilmGenereDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }
    
    public FilmGenereDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(final FilmGenereBean filmGenere) {
        final String selectQuery = "SELECT 1 FROM Film_Genere WHERE ID_Film = ? AND Nome_Genere = ?";
        final String insertQuery = "INSERT INTO Film_Genere (ID_Film, Nome_Genere) VALUES (?, ?)";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement selectPs = connection.prepareStatement(selectQuery);
             final PreparedStatement insertPs = connection.prepareStatement(insertQuery)) {

            selectPs.setInt(1, filmGenere.getIdFilm());
            selectPs.setString(2, filmGenere.getNomeGenere());

            try (final ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) {
                    insertPs.setInt(1, filmGenere.getIdFilm());
                    insertPs.setString(2, filmGenere.getNomeGenere());
                    insertPs.executeUpdate();
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FilmGenereBean> findByIdFilm(final int idFilm) {
        final String query = "SELECT * FROM Film_Genere WHERE ID_Film = ?";
        final List<FilmGenereBean> generi = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final FilmGenereBean fg = new FilmGenereBean();
                    fg.setIdFilm(rs.getInt("ID_Film"));
                    fg.setNomeGenere(rs.getString("Nome_Genere"));
                    generi.add(fg);
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return generi;
    }
/*
    public void delete(final int idFilm, final String nomeGenere) {
        final String query = "DELETE FROM Film_Genere WHERE ID_Film = ? AND Nome_Genere = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);
            ps.setString(2, nomeGenere);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
    */

    public void deleteByIdFilm(final int idFilm) {
        final String query = "DELETE FROM Film_Genere WHERE ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, idFilm);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
