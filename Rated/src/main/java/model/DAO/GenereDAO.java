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

import model.Entity.GenereBean;

public class GenereDAO {

    private DataSource dataSource;

    public GenereDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }
    
    // Costruttore aggiunto per i Test
    public GenereDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void save(final GenereBean genere) {
        final String selectQuery = "SELECT 1 FROM Genere WHERE Nome = ?";
        final String insertQuery = "INSERT INTO Genere (Nome) VALUES (?)";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement selectPs = connection.prepareStatement(selectQuery);
             final PreparedStatement insertPs = connection.prepareStatement(insertQuery)) {

            selectPs.setString(1, genere.getNome());

            try (final ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) {
                    insertPs.setString(1, genere.getNome());
                    insertPs.executeUpdate();
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
/*
    public GenereBean findByNome(final String nome) {
        final String query = "SELECT * FROM Genere WHERE Nome = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, nome);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final GenereBean genere = new GenereBean();
                    genere.setNome(rs.getString("Nome"));
                    return genere;
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
*/
    /*
    public List<GenereBean> findAll() {
        final String query = "SELECT * FROM Genere ORDER BY Nome";
        final List<GenereBean> generi = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final GenereBean genere = new GenereBean();
                genere.setNome(rs.getString("Nome"));
                generi.add(genere);
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return generi;
    }
    */
    public List<String> findAllString() {
        final String query = "SELECT * FROM Genere ORDER BY Nome";
        final List<String> generi = new ArrayList<String>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                generi.add(rs.getString("Nome"));
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return generi;
    }
    /*
    public void delete(final String nome) {
        final String query = "DELETE FROM Genere WHERE Nome = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, nome);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
    */
}
