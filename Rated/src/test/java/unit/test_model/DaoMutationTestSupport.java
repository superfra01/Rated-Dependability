package unit.test_model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import model.Entity.FilmBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import model.Entity.ValutazioneBean;

final class DaoMutationTestSupport {

    private static final byte[] IMAGE = {3, 1, 4};

    private DaoMutationTestSupport() {
    }

    static JdbcRow jdbcRow() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        return new JdbcRow(dataSource, connection, statement, resultSet);
    }

    static void stubFilm(ResultSet resultSet) throws SQLException {
        when(resultSet.getInt("ID_Film")).thenReturn(41);
        when(resultSet.getBytes("locandina")).thenReturn(IMAGE);
        when(resultSet.getBytes("Locandina")).thenReturn(IMAGE);
        when(resultSet.getString("nome")).thenReturn("Mutation Film");
        when(resultSet.getString("Nome")).thenReturn("Mutation Film");
        when(resultSet.getInt("anno")).thenReturn(2026);
        when(resultSet.getInt("Anno")).thenReturn(2026);
        when(resultSet.getInt("durata")).thenReturn(137);
        when(resultSet.getInt("Durata")).thenReturn(137);
        when(resultSet.getString("regista")).thenReturn("Ada Director");
        when(resultSet.getString("Regista")).thenReturn("Ada Director");
        when(resultSet.getString("attori")).thenReturn("Actor One, Actor Two");
        when(resultSet.getString("Attori")).thenReturn("Actor One, Actor Two");
        when(resultSet.getInt("valutazione")).thenReturn(87);
        when(resultSet.getInt("Valutazione")).thenReturn(87);
        when(resultSet.getString("trama")).thenReturn("A mutation-resistant plot");
        when(resultSet.getString("Trama")).thenReturn("A mutation-resistant plot");
    }

    static void assertFilm(FilmBean film) {
        assertEquals(41, film.getIdFilm());
        assertArrayEquals(IMAGE, film.getLocandina());
        assertEquals("Mutation Film", film.getNome());
        assertEquals(2026, film.getAnno());
        assertEquals(137, film.getDurata());
        assertEquals("Ada Director", film.getRegista());
        assertEquals("Actor One, Actor Two", film.getAttori());
        assertEquals(87, film.getValutazione());
        assertEquals("A mutation-resistant plot", film.getTrama());
    }

    static void stubReview(ResultSet resultSet) throws SQLException {
        when(resultSet.getString("titolo")).thenReturn("Precise review");
        when(resultSet.getString("contenuto")).thenReturn("Every mapped field is asserted");
        when(resultSet.getInt("valutazione")).thenReturn(8);
        when(resultSet.getInt("N_Like")).thenReturn(13);
        when(resultSet.getInt("N_DisLike")).thenReturn(2);
        when(resultSet.getInt("N_Reports")).thenReturn(1);
        when(resultSet.getString("email")).thenReturn("reviewer@example.com");
        when(resultSet.getInt("ID_Film")).thenReturn(41);
    }

    static void assertReview(RecensioneBean review) {
        assertEquals("Precise review", review.getTitolo());
        assertEquals("Every mapped field is asserted", review.getContenuto());
        assertEquals(8, review.getValutazione());
        assertEquals(13, review.getNLike());
        assertEquals(2, review.getNDislike());
        assertEquals(1, review.getNReports());
        assertEquals("reviewer@example.com", review.getEmail());
        assertEquals(41, review.getIdFilm());
    }

    static void stubUser(ResultSet resultSet) throws SQLException {
        when(resultSet.getString("email")).thenReturn("user@example.com");
        when(resultSet.getBytes("icona")).thenReturn(IMAGE);
        when(resultSet.getString("username")).thenReturn("mutation-user");
        when(resultSet.getString("password")).thenReturn("hashed-password");
        when(resultSet.getString("Tipo_Utente")).thenReturn("admin");
        when(resultSet.getInt("N_Warning")).thenReturn(3);
        when(resultSet.getString("Biografia")).thenReturn("Mutation testing profile");
    }

    static void assertUser(UtenteBean user) {
        assertEquals("user@example.com", user.getEmail());
        assertArrayEquals(IMAGE, user.getIcona());
        assertEquals("mutation-user", user.getUsername());
        assertEquals("hashed-password", user.getPassword());
        assertEquals("admin", user.getTipoUtente());
        assertEquals(3, user.getNWarning());
        assertEquals("Mutation testing profile", user.getBiografia());
    }

    static void stubVote(ResultSet resultSet) throws SQLException {
        when(resultSet.getBoolean("Like_Dislike")).thenReturn(true);
        when(resultSet.getString("email")).thenReturn("voter@example.com");
        when(resultSet.getString("email_Recensore")).thenReturn("reviewer@example.com");
        when(resultSet.getInt("ID_Film")).thenReturn(41);
    }

    static void assertVote(ValutazioneBean vote) {
        assertTrue(vote.isLikeDislike());
        assertEquals("voter@example.com", vote.getEmail());
        assertEquals("reviewer@example.com", vote.getEmailRecensore());
        assertEquals(41, vote.getIdFilm());
    }

    private static void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }

    record JdbcRow(DataSource dataSource, Connection connection,
                   PreparedStatement statement, ResultSet resultSet) {
    }
}
