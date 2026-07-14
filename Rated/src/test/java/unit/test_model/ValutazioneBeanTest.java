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

class ValutazioneBeanTest {

    @Test
    void valutazioneBeanCoversConstructorsAndAccessors() {
        ValutazioneBean rating = new ValutazioneBean();
        assertFalse(rating.isLikeDislike());
        assertEquals("", rating.getEmail());
        assertEquals("", rating.getEmailRecensore());
        assertEquals(0, rating.getIdFilm());
        rating = new ValutazioneBean(true, "voter@x.it", "author@x.it", 14);
        assertTrue(rating.isLikeDislike());
        assertEquals("voter@x.it", rating.getEmail());
        assertEquals("author@x.it", rating.getEmailRecensore());
        assertEquals(14, rating.getIdFilm());
        rating.setLikeDislike(false);
        rating.setEmail("v2@x.it");
        rating.setEmailRecensore("a2@x.it");
        rating.setIdFilm(15);
        assertFalse(rating.isLikeDislike());
        assertEquals("v2@x.it", rating.getEmail());
        assertEquals("a2@x.it", rating.getEmailRecensore());
        assertEquals(15, rating.getIdFilm());
    }
}
