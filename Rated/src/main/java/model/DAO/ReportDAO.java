package model.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import model.Entity.ReportBean;

public class ReportDAO {

    private DataSource dataSource;

    public ReportDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }
    
    public ReportDAO(final DataSource testDataSource) { // Parametro final
    	dataSource = testDataSource;
	}

    protected ReportDAO(final boolean testMode) { // Parametro final
    	
    }

    public void save(final ReportBean report) { // Parametro final
        final String query = "INSERT INTO Report (email, email_Recensore, ID_Film) VALUES (?, ?, ?)";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, report.getEmail());
            ps.setString(2, report.getEmailRecensore());
            ps.setInt(3, report.getIdFilm());
            ps.executeUpdate();
        }catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public ReportBean findById(final String email, final String emailRecensore, final int idFilm) { // Parametri final
        final String query = "SELECT * FROM Report WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, emailRecensore);
            ps.setInt(3, idFilm);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final ReportBean report = new ReportBean();
                    report.setEmail(rs.getString("email"));
                    report.setEmailRecensore(rs.getString("email_Recensore"));
                    report.setIdFilm(rs.getInt("ID_Film"));
                    return report;
                }
            }
        }catch (final SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
/*
    public void delete(final String email, final String emailRecensore, final int idFilm) { // Parametri final
        final String query = "DELETE FROM Report WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, emailRecensore);
            ps.setInt(3, idFilm);
            ps.executeUpdate();
        }catch (final SQLException e) {
            e.printStackTrace();
        }
    }
*/
    public void deleteReports(final String emailRecensore, final int idFilm) { // Parametri final
        final String query = "DELETE FROM Report WHERE email_Recensore = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, emailRecensore);
            ps.setInt(2, idFilm);
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}