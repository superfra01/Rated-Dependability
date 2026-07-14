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

class GenereBeanTest {

    @Test
    void genereBeanCoversConstructorsAndAccessors() {
        GenereBean genre = new GenereBean();
        assertEquals("", genre.getNome());
        genre = new GenereBean("Thriller");
        assertEquals("Thriller", genre.getNome());
        genre.setNome("Fantasy");
        assertEquals("Fantasy", genre.getNome());
    }
}
