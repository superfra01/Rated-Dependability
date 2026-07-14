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

class PreferenzaBeanTest {

    @Test
    void preferenzaBeanCoversConstructorsAndAccessors() {
        PreferenzaBean preference = new PreferenzaBean();
        assertEquals("", preference.getEmail());
        assertEquals("", preference.getNomeGenere());
        preference = new PreferenzaBean("a@b.it", "Horror");
        assertEquals("a@b.it", preference.getEmail());
        assertEquals("Horror", preference.getNomeGenere());
        preference.setEmail("c@d.it");
        preference.setNomeGenere("Comedy");
        assertEquals("c@d.it", preference.getEmail());
        assertEquals("Comedy", preference.getNomeGenere());
    }
}
