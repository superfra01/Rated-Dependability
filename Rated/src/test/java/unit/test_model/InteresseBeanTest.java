package unit.test_model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.GenereBean;
import model.Entity.InteresseBean;
import model.Entity.PreferenzaBean;
import model.Entity.RecensioneBean;
import model.Entity.ReportBean;
import model.Entity.UtenteBean;
import model.Entity.ValutazioneBean;
import model.Entity.VistoBean;

class InteresseBeanTest {

    @Test
    void interesseBeanCoversConstructorsAndAccessors() {
        InteresseBean interest = new InteresseBean();
        assertEquals("", interest.getEmail());
        assertEquals(0, interest.getIdFilm());
        assertFalse(interest.isInteresse());
        interest = new InteresseBean("a@b.it", 21, true);
        assertEquals("a@b.it", interest.getEmail());
        assertEquals(21, interest.getIdFilm());
        assertTrue(interest.isInteresse());
        interest.setEmail("c@d.it");
        interest.setIdFilm(22);
        interest.setInteresse(false);
        assertEquals("c@d.it", interest.getEmail());
        assertEquals(22, interest.getIdFilm());
        assertFalse(interest.isInteresse());
    }
}
