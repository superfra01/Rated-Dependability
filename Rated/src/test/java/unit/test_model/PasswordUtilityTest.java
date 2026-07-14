package unit.test_model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import utilities.PasswordUtility;

class PasswordUtilityTest {

    @Test
    void canBeConstructed() {
        assertNotNull(new PasswordUtility());
    }

    @Test
    void hashIsDeterministicAndDependsOnInput() {
        String first = PasswordUtility.hashPassword("Pippo1234.");
        assertEquals(first, PasswordUtility.hashPassword("Pippo1234."));
        assertNotEquals(first, PasswordUtility.hashPassword("Pippo1235."));
        assertTrue(first.startsWith("c2FsYXRpbm"));
    }

    @Test
    void hashRejectsNull() {
        assertThrows(NullPointerException.class, () -> PasswordUtility.hashPassword(null));
    }

    @Test
    void hashWrapsMissingAlgorithm() {
        try (MockedStatic<MessageDigest> mocked = Mockito.mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance(anyString()))
                    .thenThrow(new NoSuchAlgorithmException("missing"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> PasswordUtility.hashPassword("Pippo1234."));

            assertTrue(exception.getMessage().contains("missing"));
        }
    }
}
