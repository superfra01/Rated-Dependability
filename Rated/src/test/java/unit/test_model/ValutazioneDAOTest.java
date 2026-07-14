package unit.test_model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.DAO.InteresseDAO;
import model.DAO.PreferenzaDAO;
import model.DAO.RecensioneDAO;
import model.DAO.ReportDAO;
import model.DAO.UtenteDAO;
import model.DAO.ValutazioneDAO;
import model.DAO.VistoDAO;

class ValutazioneDAOTest {

    private static final List<Class<?>> DAO_TYPES = List.of(ValutazioneDAO.class);

    @Test
    void everyDefaultConstructorResolvesTheJndiDataSource() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Context environment = mock(Context.class);

        try (MockedConstruction<InitialContext> ignored = mockConstruction(
                InitialContext.class,
                (context, construction) -> {
                    when(context.lookup("java:comp/env")).thenReturn(environment);
                    when(environment.lookup("jdbc/RatedDB")).thenReturn(dataSource);
                })) {
            for (Class<?> daoType : DAO_TYPES) {
                Object dao = daoType.getConstructor().newInstance();
                assertNotNull(dao);
                assertEquals(dataSource, dataSourceField(daoType).get(dao));
            }
        }
    }

    @Test
    void everyDefaultConstructorWrapsJndiFailures() {
        try (MockedConstruction<InitialContext> ignored = mockConstruction(
                InitialContext.class,
                (context, construction) -> when(context.lookup("java:comp/env"))
                        .thenThrow(new NamingException("JNDI unavailable")))) {
            for (Class<?> daoType : DAO_TYPES) {
                InvocationTargetException exception = org.junit.jupiter.api.Assertions.assertThrows(
                        InvocationTargetException.class,
                        () -> daoType.getConstructor().newInstance());
                RuntimeException cause = assertInstanceOf(RuntimeException.class, exception.getCause());
                assertTrue(cause.getMessage().startsWith("Error initializing DataSource:"));
            }
        }
    }

    @Test
    void protectedVerificationConstructorsAreReachableOnlyForFormalVerification() throws Exception {
        for (Class<?> daoType : DAO_TYPES) {
            Constructor<?> constructor;
            try {
                constructor = daoType.getDeclaredConstructor(boolean.class);
            } catch (NoSuchMethodException absentVerificationConstructor) {
                continue;
            }
            constructor.setAccessible(true);
            Object dao = constructor.newInstance(true);
            assertNull(dataSourceField(daoType).get(dao));
        }
    }

    @Test
    void allDaoOperationsHandleUnavailableDatabaseWithoutEscapingSqlExceptions() throws Exception {
        DataSource unavailable = mock(DataSource.class);
        when(unavailable.getConnection()).thenThrow(new SQLException("database unavailable"));

        for (Class<?> daoType : DAO_TYPES) {
            Object dao = daoType.getConstructor(DataSource.class).newInstance(unavailable);
            for (Method method : daoType.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()
                        || method.getName().equals("setDataSource")) {
                    continue;
                }

                Object[] arguments = java.util.Arrays.stream(method.getParameterTypes())
                        .map(ValutazioneDAOTest::sampleValue)
                        .toArray();
                assertDoesNotThrow(() -> {
                    try {
                        Object result = method.invoke(dao, arguments);
                        if (List.class.isAssignableFrom(method.getReturnType())) {
                            assertNotNull(result);
                        }
                    } catch (InvocationTargetException exception) {
                        fail("SQL failure escaped from " + daoType.getSimpleName() + "." + method.getName(),
                                exception.getCause());
                    }
                });
            }
        }
    }

    private static Field dataSourceField(Class<?> daoType) throws Exception {
        Field field = daoType.getDeclaredField("dataSource");
        field.setAccessible(true);
        return field;
    }

    private static Object sampleValue(Class<?> type) {
        if (type == String.class) {
            return "value@example.com";
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == boolean.class || type == Boolean.class) {
            return true;
        }
        if (type == byte[].class) {
            return new byte[] {1};
        }
        if (type == String[].class) {
            return new String[] {"Drama"};
        }
        if (List.class.isAssignableFrom(type)) {
            return Collections.emptyList();
        }
        try {
            return type.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("No sample value for " + type.getName(), exception);
        }
    }
    @Test
    void saveDeletesAnIdenticalExistingVote() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        java.sql.PreparedStatement select = mock(java.sql.PreparedStatement.class);
        java.sql.PreparedStatement insert = mock(java.sql.PreparedStatement.class);
        java.sql.PreparedStatement update = mock(java.sql.PreparedStatement.class);
        java.sql.PreparedStatement delete = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet resultSet = mock(java.sql.ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(select, insert, update, delete);
        when(select.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("Like_Dislike")).thenReturn(true);
        model.Entity.ValutazioneBean vote = new model.Entity.ValutazioneBean();
        vote.setEmail("user@example.com");
        vote.setEmailRecensore("author@example.com");
        vote.setIdFilm(7);
        vote.setLikeDislike(true);

        new ValutazioneDAO(dataSource).save(vote);

        org.mockito.Mockito.verify(delete).setString(1, "user@example.com");
        org.mockito.Mockito.verify(delete).setString(2, "author@example.com");
        org.mockito.Mockito.verify(delete).setInt(3, 7);
        org.mockito.Mockito.verify(delete).executeUpdate();
        org.mockito.Mockito.verify(update, org.mockito.Mockito.never()).executeUpdate();
        org.mockito.Mockito.verify(insert, org.mockito.Mockito.never()).executeUpdate();
    }

    @Test
    void saveUpdatesEveryParameterWhenExistingVoteChanges() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        java.sql.PreparedStatement select = mock(java.sql.PreparedStatement.class);
        java.sql.PreparedStatement insert = mock(java.sql.PreparedStatement.class);
        java.sql.PreparedStatement update = mock(java.sql.PreparedStatement.class);
        java.sql.PreparedStatement delete = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet resultSet = mock(java.sql.ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(select, insert, update, delete);
        when(select.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("Like_Dislike")).thenReturn(false);
        model.Entity.ValutazioneBean vote = new model.Entity.ValutazioneBean();
        vote.setEmail("user@example.com");
        vote.setEmailRecensore("author@example.com");
        vote.setIdFilm(7);
        vote.setLikeDislike(true);

        new ValutazioneDAO(dataSource).save(vote);

        org.mockito.Mockito.verify(update).setBoolean(1, true);
        org.mockito.Mockito.verify(update).setString(2, "user@example.com");
        org.mockito.Mockito.verify(update).setString(3, "author@example.com");
        org.mockito.Mockito.verify(update).setInt(4, 7);
        org.mockito.Mockito.verify(update).executeUpdate();
        org.mockito.Mockito.verify(delete, org.mockito.Mockito.never()).executeUpdate();
        org.mockito.Mockito.verify(insert, org.mockito.Mockito.never()).executeUpdate();
    }

    @Test
    void findByIdMapsEveryVoteColumn() throws Exception {
        DaoMutationTestSupport.JdbcRow row = DaoMutationTestSupport.jdbcRow();
        DaoMutationTestSupport.stubVote(row.resultSet());

        model.Entity.ValutazioneBean vote = new ValutazioneDAO(row.dataSource())
                .findById("voter@example.com", "reviewer@example.com", 41);

        assertNotNull(vote);
        DaoMutationTestSupport.assertVote(vote);
    }

    @Test
    void findByFilmAndEmailMapsEveryVoteColumn() throws Exception {
        DaoMutationTestSupport.JdbcRow row = DaoMutationTestSupport.jdbcRow();
        DaoMutationTestSupport.stubVote(row.resultSet());

        java.util.HashMap<String, model.Entity.ValutazioneBean> votes =
                new ValutazioneDAO(row.dataSource()).findByIdFilmAndEmail(41, "voter@example.com");

        assertEquals(1, votes.size());
        DaoMutationTestSupport.assertVote(votes.get("reviewer@example.com"));
    }

    @Test
    void deleteValutazioniBindsReviewerAndFilm() throws Exception {
        DaoMutationTestSupport.JdbcRow row = DaoMutationTestSupport.jdbcRow();

        new ValutazioneDAO(row.dataSource()).deleteValutazioni("reviewer@example.com", 41);

        org.mockito.Mockito.verify(row.statement()).setString(1, "reviewer@example.com");
        org.mockito.Mockito.verify(row.statement()).setInt(2, 41);
        org.mockito.Mockito.verify(row.statement()).executeUpdate();
    }
}
