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

class VistoBeanTest {

    @Test
    void vistoBeanCoversConstructorsAndAccessors() {
        VistoBean watched = new VistoBean();
        assertEquals("", watched.getEmail());
        assertEquals(0, watched.getIdFilm());
        watched = new VistoBean("a@b.it", 5);
        assertEquals("a@b.it", watched.getEmail());
        assertEquals(5, watched.getIdFilm());
        watched.setEmail("c@d.it");
        watched.setIdFilm(6);
        assertEquals("c@d.it", watched.getEmail());
        assertEquals(6, watched.getIdFilm());
    }
}
