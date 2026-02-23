package model.Entity;

import java.io.Serializable;

public class FilmGenereBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idFilm;
    private String nomeGenere;

    public FilmGenereBean() {
        idFilm = 0;
        nomeGenere = "";
    }

    public FilmGenereBean(final int idFilm, final String nomeGenere) {
        this.idFilm = idFilm;
        this.nomeGenere = nomeGenere;
    }

    public int getIdFilm() {
        return idFilm;
    }

    public void setIdFilm(final int idFilm) {
        this.idFilm = idFilm;
    }

    public String getNomeGenere() {
        return nomeGenere;
    }

    public void setNomeGenere(final String nomeGenere) {
        this.nomeGenere = nomeGenere;
    }
}
