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

    private DataSource dataSource; // Non può essere final perché c'è un setter

    public UtenteDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }
    
    // Costruttore per test (iniezione di DataSource mock)
    public UtenteDAO(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    protected UtenteDAO(final boolean testMode) {
        // Vuoto: non fa nulla, niente DB!
    }

    // Metodo setter per cambiare il DataSource
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

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
            e.printStackTrace();
        }
    }

    public UtenteBean findByEmail(final String email) {
        final String query = "SELECT * FROM Utente_Registrato WHERE email = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final UtenteBean utente = new UtenteBean();
                    utente.setEmail(rs.getString("email"));
                    utente.setIcona(rs.getBytes("icona"));
                    utente.setUsername(rs.getString("username"));
                    utente.setPassword(rs.getString("password"));
                    utente.setTipoUtente(rs.getString("Tipo_Utente"));
                    utente.setNWarning(rs.getInt("N_Warning"));
                    utente.setBiografia(rs.getString("Biografia"));
                    return utente;
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UtenteBean findByUsername(final String username) {
        final String query = "SELECT * FROM Utente_Registrato WHERE username = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final UtenteBean user = new UtenteBean();
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setTipoUtente(rs.getString("Tipo_Utente"));
                    user.setIcona(rs.getBytes("icona"));
                    user.setNWarning(rs.getInt("N_Warning"));
                    user.setBiografia(rs.getString("Biografia"));
                    return user;
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
/*
    public List<UtenteBean> findAll() {
        final String query = "SELECT * FROM Utente_Registrato";
        final List<UtenteBean> utenti = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query);
             final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final UtenteBean utente = new UtenteBean();
                utente.setEmail(rs.getString("email"));
                utente.setIcona(rs.getBytes("icona"));
                utente.setUsername(rs.getString("username"));
                utente.setPassword(rs.getString("password"));
                utente.setTipoUtente(rs.getString("Tipo_Utente"));
                utente.setNWarning(rs.getInt("N_Warning"));
                utente.setBiografia(rs.getString("Biografia"));
                utenti.add(utente);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return utenti;
    }
*/
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
            e.printStackTrace();
        }
    }
/*
    public void delete(final String email) {
        final String query = "DELETE FROM Utente_Registrato WHERE email = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
*/
}