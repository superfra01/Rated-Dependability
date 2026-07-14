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

class RecensioneBeanTest {

    @Test
    void recensioneBeanCoversConstructorsAndAccessors() {
        RecensioneBean empty = new RecensioneBean();
        assertEquals("", empty.getTitolo());
        assertEquals("", empty.getContenuto());
        assertEquals(0, empty.getValutazione());
        assertEquals(0, empty.getNLike());
        assertEquals(0, empty.getNDislike());
        assertEquals(0, empty.getNReports());
        assertEquals("", empty.getEmail());
        assertEquals(0, empty.getIdFilm());

        RecensioneBean review = new RecensioneBean("Titolo", "Testo", 4, 10, 2, 1, "a@b.it", 9);
        assertEquals("Titolo", review.getTitolo());
        assertEquals("Testo", review.getContenuto());
        assertEquals(4, review.getValutazione());
        assertEquals(10, review.getNLike());
        assertEquals(2, review.getNDislike());
        assertEquals(1, review.getNReports());
        assertEquals("a@b.it", review.getEmail());
        assertEquals(9, review.getIdFilm());

        review.setTitolo("T2");
        review.setContenuto("C2");
        review.setValutazione(5);
        review.setNLike(11);
        review.setNDislike(3);
        review.setNReports(2);
        review.setEmail("c@d.it");
        review.setIdFilm(10);
        assertEquals("T2", review.getTitolo());
        assertEquals("C2", review.getContenuto());
        assertEquals(5, review.getValutazione());
        assertEquals(11, review.getNLike());
        assertEquals(3, review.getNDislike());
        assertEquals(2, review.getNReports());
        assertEquals("c@d.it", review.getEmail());
        assertEquals(10, review.getIdFilm());
    }
}
