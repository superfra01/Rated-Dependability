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
import model.Entity.UtenteBean;

public class UtenteDAO {

    //@ spec_public
    private DataSource dataSource; 
    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    //@ assignable \nothing;
    //@ skipesc
    public UtenteDAO() {
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
    //@ assignable \nothing;
    public UtenteDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*@ 
      @ requires testMode == true;
      @ skipesc
      @*/
    protected UtenteDAO(final boolean testMode) {
        // Vuoto
    }

    //@ requires dataSource != null;
    //@ assignable this.dataSource;
    //@ ensures this.dataSource == dataSource;
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */

    //@ requires dataSource != null;
    //@ requires utente != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void save(final UtenteBean utente) {
        final String query = "INSERT INTO Utente_Registrato (email, icona, username, password, Tipo_Utente, N_Warning, Biografia) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, utente.getEmail());
            ps.setBytes(2, utente.getIcona());
            ps.setString(3, utente.getUsername());
            ps.setString(4, utente.getPassword());
            ps.setString(5, utente.getTipoUtente());
            ps.setInt(6, utente.getNWarning());
            ps.setString(7, utente.getBiografia());
            ps.executeUpdate();
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
        }
    }

    //@ requires dataSource != null;
    //@ requires email != null;
    //@ assignable \nothing;
    //@ ensures \result != null ==> \result.getEmail().equals(email);
    //@ skipesc
    //@ skiprac
    public /*@ nullable @*/ UtenteBean findByEmail(final String email) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT email, icona, username, password, Tipo_Utente, N_Warning, Biografia FROM Utente_Registrato WHERE email = ?";
        UtenteBean result = null;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = new UtenteBean();
                    result.setEmail(rs.getString("email"));
                    result.setIcona(rs.getBytes("icona"));
                    result.setUsername(rs.getString("username"));
                    result.setPassword(rs.getString("password"));
                    result.setTipoUtente(rs.getString("Tipo_Utente"));
                    result.setNWarning(rs.getInt("N_Warning"));
                    result.setBiografia(rs.getString("Biografia"));
                }
            }
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
            return null;
        }
        return result;
    }

    //@ requires dataSource != null;
    //@ requires username != null;
    //@ assignable \nothing;
    //@ ensures \result != null ==> \result.getUsername().equals(username);
    //@ skipesc
    //@ skiprac
    public /*@ nullable @*/ UtenteBean findByUsername(final String username) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT email, icona, username, password, Tipo_Utente, N_Warning, Biografia FROM Utente_Registrato WHERE username = ?";
        UtenteBean result = null;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = new UtenteBean();
                    result.setUsername(rs.getString("username"));
                    result.setEmail(rs.getString("email"));
                    result.setPassword(rs.getString("password"));
                    result.setTipoUtente(rs.getString("Tipo_Utente"));
                    result.setIcona(rs.getBytes("icona"));
                    result.setNWarning(rs.getInt("N_Warning"));
                    result.setBiografia(rs.getString("Biografia"));
                }
            }
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
            return null;
        }
        return result;
    }

    //@ requires dataSource != null;
    //@ requires utente != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void update(final UtenteBean utente) {
        final String query = "UPDATE Utente_Registrato SET icona = ?, username = ?, password = ?, Tipo_Utente = ?, N_Warning = ?, Biografia = ? WHERE email = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setBytes(1, utente.getIcona());
            ps.setString(2, utente.getUsername());
            ps.setString(3, utente.getPassword());
            ps.setString(4, utente.getTipoUtente());
            ps.setInt(5, utente.getNWarning());
            ps.setString(6, utente.getBiografia());
            ps.setString(7, utente.getEmail());
            ps.executeUpdate();
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
        }
    }
}
