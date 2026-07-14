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

class FilmGenereBeanTest {

    @Test
    void filmGenereBeanCoversConstructorsAndAccessors() {
        FilmGenereBean filmGenre = new FilmGenereBean();
        assertEquals(0, filmGenre.getIdFilm());
        assertEquals("", filmGenre.getNomeGenere());
        filmGenre = new FilmGenereBean(3, "Drama");
        assertEquals(3, filmGenre.getIdFilm());
        assertEquals("Drama", filmGenre.getNomeGenere());
        filmGenre.setIdFilm(4);
        filmGenre.setNomeGenere("Azione");
        assertEquals(4, filmGenre.getIdFilm());
        assertEquals("Azione", filmGenre.getNomeGenere());
    }
}
