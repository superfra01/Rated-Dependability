package unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import utilities.FieldValidator;

class FieldValidatorTest {

    @Test
    void acceptsPasswordWithTrailingPeriod() {
        assertTrue(FieldValidator.validatePassword("Pippo1234."));
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {
        assertFalse(FieldValidator.validatePassword("Pippo1234"));
    }
}
