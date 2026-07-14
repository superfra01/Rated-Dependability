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

class FilmBeanTest {

    @Test
    void filmBeanCoversConstructorsAndAccessors() {
        FilmBean empty = new FilmBean();
        assertEquals(0, empty.getIdFilm());
        assertNull(empty.getLocandina());
        assertEquals("", empty.getNome());
        assertEquals(0, empty.getAnno());
        assertEquals(0, empty.getDurata());
        assertEquals("", empty.getRegista());
        assertEquals("", empty.getAttori());
        assertEquals(1, empty.getValutazione());
        assertEquals("", empty.getTrama());

        byte[] poster = {1, 2, 3};
        FilmBean film = new FilmBean(7, poster, "Film", 2025, 123, "Regista", "Cast", "Trama");
        assertEquals(7, film.getIdFilm());
        assertArrayEquals(poster, film.getLocandina());
        assertEquals("Film", film.getNome());
        assertEquals(2025, film.getAnno());
        assertEquals(123, film.getDurata());
        assertEquals("Regista", film.getRegista());
        assertEquals("Cast", film.getAttori());
        assertEquals(1, film.getValutazione());
        assertEquals("Trama", film.getTrama());

        byte[] replacement = {9, 8};
        film.setIdFilm(8);
        film.setLocandina(replacement);
        film.setNome("Nuovo");
        film.setAnno(2026);
        film.setDurata(99);
        film.setRegista("Nuovo regista");
        film.setAttori("Nuovo cast");
        film.setValutazione(5);
        film.setTrama("Nuova trama");
        assertEquals(8, film.getIdFilm());
        assertArrayEquals(replacement, film.getLocandina());
        assertEquals("Nuovo", film.getNome());
        assertEquals(2026, film.getAnno());
        assertEquals(99, film.getDurata());
        assertEquals("Nuovo regista", film.getRegista());
        assertEquals("Nuovo cast", film.getAttori());
        assertEquals(5, film.getValutazione());
        assertEquals("Nuova trama", film.getTrama());
    }
}
