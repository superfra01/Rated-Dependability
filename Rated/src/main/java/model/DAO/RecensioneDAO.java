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
import model.Entity.RecensioneBean;

public class RecensioneDAO {

    private DataSource dataSource;

    public RecensioneDAO() {
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            this.dataSource = (DataSource) envCtx.lookup("jdbc/RatedDB");
        } catch (final NamingException e) {
            throw new RuntimeException("Error initializing DataSource: " + e.getMessage());
        }
    }

    public RecensioneDAO(final DataSource testDataSource) { // Parametro final
		dataSource = testDataSource;
	}
    
    protected RecensioneDAO(final boolean testMode) { // Parametro final
        // Non fare nulla qui! Niente connessione al DB.
    }

	public void save(final RecensioneBean recensione) { // Parametro final
        final String query = "INSERT INTO Recensione (titolo, contenuto, valutazione, N_Like, N_DisLike, N_Reports, email, ID_Film) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, recensione.getTitolo());
            ps.setString(2, recensione.getContenuto());
            ps.setInt(3, recensione.getValutazione());
            ps.setInt(4, recensione.getNLike());
            ps.setInt(5, recensione.getNDislike());
            ps.setInt(6, recensione.getNReports());
            ps.setString(7, recensione.getEmail());
            ps.setInt(8, recensione.getIdFilm());
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public RecensioneBean findById(final String email, final int idFilm) { // Parametri final
        final String query = "SELECT * FROM Recensione WHERE email = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final RecensioneBean recensione = new RecensioneBean();
                    recensione.setTitolo(rs.getString("titolo"));
                    recensione.setContenuto(rs.getString("contenuto"));
                    recensione.setValutazione(rs.getInt("valutazione"));
                    recensione.setNLike(rs.getInt("N_Like"));
                    recensione.setNDislike(rs.getInt("N_DisLike"));
                    recensione.setNReports(rs.getInt("N_Reports"));
                    recensione.setEmail(rs.getString("email"));
                    recensione.setIdFilm(rs.getInt("ID_Film"));
                    return recensione;
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<RecensioneBean> findByIdFilm(final int idFilm) { // Parametro final
        final String query = "SELECT * FROM Recensione WHERE ID_Film = ?";
        final List<RecensioneBean> recensioni = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idFilm);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RecensioneBean recensione = new RecensioneBean();
                    recensione.setTitolo(rs.getString("titolo"));
                    recensione.setContenuto(rs.getString("contenuto"));
                    recensione.setValutazione(rs.getInt("valutazione"));
                    recensione.setNLike(rs.getInt("N_Like"));
                    recensione.setNDislike(rs.getInt("N_DisLike"));
                    recensione.setNReports(rs.getInt("N_Reports"));
                    recensione.setEmail(rs.getString("email"));
                    recensione.setIdFilm(rs.getInt("ID_Film"));
                    recensioni.add(recensione);
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return recensioni;
    }

    public List<RecensioneBean> findAll() {
        final String query = "SELECT * FROM Recensione";
        final List<RecensioneBean> recensioni = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query);
             final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final RecensioneBean recensione = new RecensioneBean();
                recensione.setTitolo(rs.getString("titolo"));
                recensione.setContenuto(rs.getString("contenuto"));
                recensione.setValutazione(rs.getInt("valutazione"));
                recensione.setNLike(rs.getInt("N_Like"));
                recensione.setNDislike(rs.getInt("N_DisLike"));
                recensione.setNReports(rs.getInt("N_Reports"));
                recensione.setEmail(rs.getString("email"));
                recensione.setIdFilm(rs.getInt("ID_Film"));
                recensioni.add(recensione);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return recensioni;
    }
    
    public List<RecensioneBean> findByUser(final String email) { // Parametro final
        final String query = "SELECT * FROM Recensione WHERE email = ?";
        final List<RecensioneBean> recensioni = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RecensioneBean recensione = new RecensioneBean();
                    recensione.setTitolo(rs.getString("titolo"));
                    recensione.setContenuto(rs.getString("contenuto"));
                    recensione.setValutazione(rs.getInt("valutazione"));
                    recensione.setNLike(rs.getInt("N_Like"));
                    recensione.setNDislike(rs.getInt("N_DisLike"));
                    recensione.setNReports(rs.getInt("N_Reports"));
                    recensione.setEmail(rs.getString("email"));
                    recensione.setIdFilm(rs.getInt("ID_Film"));
                    recensioni.add(recensione);
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return recensioni;
    }
    
    public void update(final RecensioneBean recensione) { // Parametro final
        final String query = "UPDATE Recensione SET titolo = ?, contenuto = ?, valutazione = ?, N_Like = ?, N_DisLike = ?, N_Reports = ? WHERE email = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, recensione.getTitolo());
            ps.setString(2, recensione.getContenuto());
            ps.setInt(3, recensione.getValutazione());
            ps.setInt(4, recensione.getNLike());
            ps.setInt(5, recensione.getNDislike());
            ps.setInt(6, recensione.getNReports());
            ps.setString(7, recensione.getEmail());
            ps.setInt(8, recensione.getIdFilm());
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(final String email, final int idFilm) { // Parametri final
        final String query = "DELETE FROM Recensione WHERE email = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}