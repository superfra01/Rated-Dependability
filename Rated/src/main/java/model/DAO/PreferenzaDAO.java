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

import model.Entity.PreferenzaBean;

public class PreferenzaDAO {

    //@ spec_public
    private final DataSource dataSource; 

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant dataSource != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    public PreferenzaDAO() {
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
    public PreferenzaDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /*@ 
      @ requires testMode == true;
      @ skipesc
      @*/
    protected PreferenzaDAO(final boolean testMode) {
        this.dataSource = null; 
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */

    //@ requires preferenza != null;
    //@ assignable \everything;
    public void save(final PreferenzaBean preferenza) {
        final String selectQuery = "SELECT 1 FROM Preferenza WHERE email = ? AND Nome_Genere = ?";
        final String insertQuery = "INSERT INTO Preferenza (email, Nome_Genere) VALUES (?, ?)";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement selectPs = connection.prepareStatement(selectQuery);
             final PreparedStatement insertPs = connection.prepareStatement(insertQuery)) {

            selectPs.setString(1, preferenza.getEmail());
            selectPs.setString(2, preferenza.getNomeGenere());

            try (final ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) {
                    insertPs.setString(1, preferenza.getEmail());
                    insertPs.setString(2, preferenza.getNomeGenere());
                    insertPs.executeUpdate();
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    //@ requires email != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public List<PreferenzaBean> findByEmail(final String email) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT email, Nome_Genere FROM Preferenza WHERE email = ?";
        final List<PreferenzaBean> preferenze = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, email);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final PreferenzaBean p = new PreferenzaBean(); 
                    p.setEmail(rs.getString("email"));
                    p.setNomeGenere(rs.getString("Nome_Genere"));
                    preferenze.add(p);
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return preferenze;
    }

    //@ requires email != null;
    //@ assignable \everything;
    public void deleteByEmail(final String email) {
        final String query = "DELETE FROM Preferenza WHERE email = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, email);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}