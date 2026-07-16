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

    //@ spec_public
    private final DataSource dataSource;
    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.dataSource != null;
    //@ assignable \nothing;
    //@ skipesc
    public RecensioneDAO() {
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
    //@ assignable \nothing;
    public RecensioneDAO(final DataSource testDataSource) {
        this.dataSource = testDataSource;
    }

    /*@ 
      @ requires testMode == true;
      @ skipesc
      @*/
    protected RecensioneDAO(final boolean testMode) {
        this.dataSource = null; 
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */

    //@ requires dataSource != null;
    //@ requires recensione != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void save(final RecensioneBean recensione) {
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
            // Preserve the DAO fallback without exposing database details.
        }
    }

    //@ requires dataSource != null;
    //@ requires email != null;
    //@ requires idFilm >= 0;
    //@ assignable \nothing;
    //@ ensures \result != null ==> (\result.getEmail().equals(email) && \result.getIdFilm() == idFilm);
    //@ skipesc
    //@ skiprac
    public /*@ nullable @*/ RecensioneBean findById(final String email, final int idFilm) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT titolo, contenuto, valutazione, N_Like, N_DisLike, N_Reports, email, ID_Film FROM Recensione WHERE email = ? AND ID_Film = ?";
        RecensioneBean result = null;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = new RecensioneBean();
                    result.setTitolo(rs.getString("titolo"));
                    result.setContenuto(rs.getString("contenuto"));
                    result.setValutazione(rs.getInt("valutazione"));
                    result.setNLike(rs.getInt("N_Like"));
                    result.setNDislike(rs.getInt("N_DisLike"));
                    result.setNReports(rs.getInt("N_Reports"));
                    result.setEmail(rs.getString("email"));
                    result.setIdFilm(rs.getInt("ID_Film"));
                }
            }
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
            return null;
        }
        return result;
    }
    
    //@ requires dataSource != null;
    //@ requires idFilm >= 0;
    //@ assignable \nothing;
    //@ ensures \result != null;
    //@ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null && \result.get(i).getIdFilm() == idFilm);
    //@ skipesc
    //@ skiprac
    public List<RecensioneBean> findByIdFilm(final int idFilm) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT titolo, contenuto, valutazione, N_Like, N_DisLike, N_Reports, email, ID_Film FROM Recensione WHERE ID_Film = ?";
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
            // Preserve the DAO fallback without exposing database details.
        }
        return recensioni;
    }

    //@ requires dataSource != null;
    //@ assignable \nothing;
    //@ ensures \result != null;
    //@ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null);
    //@ skipesc
    //@ skiprac
    public List<RecensioneBean> findAll() {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT titolo, contenuto, valutazione, N_Like, N_DisLike, N_Reports, email, ID_Film FROM Recensione";
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
            // Preserve the DAO fallback without exposing database details.
        }
        return recensioni;
    }
    
    //@ requires dataSource != null;
    //@ requires email != null;
    //@ assignable \nothing;
    //@ ensures \result != null;
    //@ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null && \result.get(i).getEmail().equals(email));
    //@ skipesc
    //@ skiprac
    public List<RecensioneBean> findByUser(final String email) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT titolo, contenuto, valutazione, N_Like, N_DisLike, N_Reports, email, ID_Film FROM Recensione WHERE email = ?";
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
            // Preserve the DAO fallback without exposing database details.
        }
        return recensioni;
    }
    
    //@ requires dataSource != null;
    //@ requires recensione != null;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void update(final RecensioneBean recensione) {
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
            // Preserve the DAO fallback without exposing database details.
        }
    }

    //@ requires dataSource != null;
    //@ requires email != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    //@ skipesc
    //@ skiprac
    public void delete(final String email, final int idFilm) {
        final String query = "DELETE FROM Recensione WHERE email = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setInt(2, idFilm);
            ps.executeUpdate();
        } catch (final SQLException e) {
            // Preserve the DAO fallback without exposing database details.
        }
    }
}
