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

class ReportBeanTest {

    @Test
    void reportBeanCoversConstructorsAndAccessors() {
        ReportBean report = new ReportBean();
        assertEquals("", report.getEmail());
        assertEquals("", report.getEmailRecensore());
        assertEquals(0, report.getIdFilm());
        report = new ReportBean("reporter@x.it", "author@x.it", 12);
        assertEquals("reporter@x.it", report.getEmail());
        assertEquals("author@x.it", report.getEmailRecensore());
        assertEquals(12, report.getIdFilm());
        report.setEmail("r2@x.it");
        report.setEmailRecensore("a2@x.it");
        report.setIdFilm(13);
        assertEquals("r2@x.it", report.getEmail());
        assertEquals("a2@x.it", report.getEmailRecensore());
        assertEquals(13, report.getIdFilm());
    }
}
