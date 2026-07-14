package unit.test_model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import utilities.FieldValidator;

class FieldValidatorTest {

    @Test
    void canBeConstructed() {
        assertNotNull(new FieldValidator());
    }

    @Test
    void coversNullInvalidAndBoundaryInputs() {
        assertFalse(FieldValidator.validateUsername(null));
        assertFalse(FieldValidator.validateUsername("ab"));
        assertTrue(FieldValidator.validateUsername("abc"));
        assertTrue(FieldValidator.validateUsername("user_name.01"));
        assertFalse(FieldValidator.validateUsername("bad name"));

        assertFalse(FieldValidator.validateEmail(null));
        assertFalse(FieldValidator.validateEmail("invalid"));
        assertTrue(FieldValidator.validateEmail("name.surname+tag@example.co.uk"));

        assertFalse(FieldValidator.validatePassword(null));
        assertFalse(FieldValidator.validatePassword("Pippo1234"));
        assertFalse(FieldValidator.validatePassword("pippo1234."));
        assertFalse(FieldValidator.validatePassword("PIPPO1234."));
        assertFalse(FieldValidator.validatePassword("PippoTest."));
        assertTrue(FieldValidator.validatePassword("Pippo1234."));
        assertTrue(FieldValidator.validatePassword("Password123!"));
    }
}
