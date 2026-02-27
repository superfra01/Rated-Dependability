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

    //@ spec_public
    private DataSource dataSource;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant dataSource != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    public GenereDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }
    
    //@ requires dataSource != null;
    //@ ensures this.dataSource == dataSource;
    public GenereDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */
    
    //@ requires genere != null;
    //@ assignable \everything;
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

    //@ assignable \everything;
    //@ ensures \result != null;
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
}