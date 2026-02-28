package model.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import model.Entity.ValutazioneBean;

public class ValutazioneDAO {

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
    public ValutazioneDAO() {
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
    public ValutazioneDAO(final DataSource testDataSource) {
        this.dataSource = testDataSource;
    }
    
    /*@ 
      @ requires testMode == true;
      @ skipesc
      @*/
    protected ValutazioneDAO(final boolean testMode) {
        this.dataSource = null; 
    }

    /* =========================================
     * METODI CRUD
     * ========================================= */

    //@ requires valutazione != null;
    //@ assignable \everything;
    public void save(final ValutazioneBean valutazione) {
        // RISOLTO: Sostituito SELECT * con la colonna specifica necessaria al controllo logico
        final String selectQuery = "SELECT Like_Dislike FROM Valutazione WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";
        final String insertQuery = "INSERT INTO Valutazione (Like_Dislike, email, email_Recensore, ID_Film) VALUES (?, ?, ?, ?)";
        final String updateQuery = "UPDATE Valutazione SET Like_Dislike = ? WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";
        final String deleteQuery = "DELETE FROM Valutazione WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement selectPs = connection.prepareStatement(selectQuery);
             final PreparedStatement insertPs = connection.prepareStatement(insertQuery);
             final PreparedStatement updatePs = connection.prepareStatement(updateQuery);
             final PreparedStatement deletePs = connection.prepareStatement(deleteQuery)) {

            selectPs.setString(1, valutazione.getEmail());
            selectPs.setString(2, valutazione.getEmailRecensore());
            selectPs.setInt(3, valutazione.getIdFilm());

            try (final ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    final boolean existingLikeDislike = rs.getBoolean("Like_Dislike");
                    if (existingLikeDislike == valutazione.isLikeDislike()) {
                        deletePs.setString(1, valutazione.getEmail());
                        deletePs.setString(2, valutazione.getEmailRecensore());
                        deletePs.setInt(3, valutazione.getIdFilm());
                        deletePs.executeUpdate();
                    } else {
                        updatePs.setBoolean(1, valutazione.isLikeDislike());
                        updatePs.setString(2, valutazione.getEmail());
                        updatePs.setString(3, valutazione.getEmailRecensore());
                        updatePs.setInt(4, valutazione.getIdFilm());
                        updatePs.executeUpdate();
                    }
                } else {
                    insertPs.setBoolean(1, valutazione.isLikeDislike());
                    insertPs.setString(2, valutazione.getEmail());
                    insertPs.setString(3, valutazione.getEmailRecensore());
                    insertPs.setInt(4, valutazione.getIdFilm());
                    insertPs.executeUpdate();
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    //@ requires email != null;
    //@ requires emailRecensore != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    //@ ensures \result != null ==> (\result.getEmail().equals(email) && \result.getEmailRecensore().equals(emailRecensore) && \result.getIdFilm() == idFilm);
    public ValutazioneBean findById(final String email, final String emailRecensore, final int idFilm) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT Like_Dislike, email, email_Recensore, ID_Film FROM Valutazione WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, emailRecensore);
            ps.setInt(3, idFilm);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final ValutazioneBean valutazione = new ValutazioneBean(); 
                    valutazione.setLikeDislike(rs.getBoolean("Like_Dislike"));
                    valutazione.setEmail(rs.getString("email"));
                    valutazione.setEmailRecensore(rs.getString("email_Recensore"));
                    valutazione.setIdFilm(rs.getInt("ID_Film"));
                    return valutazione;
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //@ requires idFilm >= 0;
    //@ requires email != null;
    //@ assignable \everything;
    //@ ensures \result != null;
    public HashMap<String, ValutazioneBean> findByIdFilmAndEmail(final int idFilm, final String email) {
        // RISOLTO: Sostituito SELECT * con elenco esplicito delle colonne
        final String query = "SELECT Like_Dislike, email, email_Recensore, ID_Film FROM Valutazione WHERE ID_Film = ? AND email = ?";
        final HashMap<String, ValutazioneBean> valutazioni = new HashMap<>(); 
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idFilm);
            ps.setString(2, email);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final ValutazioneBean valutazione = new ValutazioneBean(); 
                    valutazione.setLikeDislike(rs.getBoolean("Like_Dislike"));
                    valutazione.setEmail(rs.getString("email"));
                    valutazione.setEmailRecensore(rs.getString("email_Recensore"));
                    valutazione.setIdFilm(rs.getInt("ID_Film"));
                    valutazioni.put(rs.getString("email_Recensore"), valutazione);
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return valutazioni;
    }
    
    //@ requires email != null;
    //@ requires emailRecensore != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public void delete(final String email, final String emailRecensore, final int idFilm) {
        final String query = "DELETE FROM Valutazione WHERE email = ? AND email_Recensore = ? AND ID_Film = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, emailRecensore);
            ps.setInt(3, idFilm);
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    //@ requires emailRecensore != null;
    //@ requires idFilm >= 0;
    //@ assignable \everything;
    public void deleteValutazioni(final String emailRecensore, final int idFilm) {
        final String query = "DELETE FROM Valutazione WHERE email_Recensore = ? AND ID_Film = ?";
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