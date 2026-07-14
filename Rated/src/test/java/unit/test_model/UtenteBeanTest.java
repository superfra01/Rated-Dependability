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

class UtenteBeanTest {

    @Test
    void utenteBeanCoversConstructorsAndAccessors() {
        UtenteBean empty = new UtenteBean();
        assertEquals("", empty.getEmail());
        assertNull(empty.getIcona());
        assertEquals("", empty.getUsername());
        assertEquals("", empty.getPassword());
        assertEquals("", empty.getTipoUtente());
        assertEquals(0, empty.getNWarning());
        assertEquals("", empty.getBiografia());

        byte[] icon = {4, 5};
        UtenteBean user = new UtenteBean("a@b.it", icon, "utente", "hash", "RECENSORE", 2, "bio");
        assertEquals("a@b.it", user.getEmail());
        assertArrayEquals(icon, user.getIcona());
        assertEquals("utente", user.getUsername());
        assertEquals("hash", user.getPassword());
        assertEquals("RECENSORE", user.getTipoUtente());
        assertEquals(2, user.getNWarning());
        assertEquals("bio", user.getBiografia());

        byte[] replacement = {7};
        user.setEmail("c@d.it");
        user.setIcona(replacement);
        user.setUsername("nuovo");
        user.setPassword("nuovo-hash");
        user.setTipoUtente("GESTORE");
        user.setNWarning(3);
        user.setBiografia("nuova bio");
        assertEquals("c@d.it", user.getEmail());
        assertArrayEquals(replacement, user.getIcona());
        assertEquals("nuovo", user.getUsername());
        assertEquals("nuovo-hash", user.getPassword());
        assertEquals("GESTORE", user.getTipoUtente());
        assertEquals(3, user.getNWarning());
        assertEquals("nuova bio", user.getBiografia());
    }
}
