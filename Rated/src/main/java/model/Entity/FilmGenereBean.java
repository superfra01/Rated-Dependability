package model.Entity;

import java.io.Serializable;

public class FilmGenereBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private int idFilm;
    
    //@ spec_public
    private String nomeGenere;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant idFilm >= 0;
    //@ public invariant nomeGenere != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.idFilm == 0;
    //@ ensures this.nomeGenere.equals("");
    public FilmGenereBean() {
        idFilm = 0;
        nomeGenere = "";
    }

    //@ requires idFilm >= 0;
    //@ requires nomeGenere != null;
    //@ ensures this.idFilm == idFilm;
    //@ ensures this.nomeGenere == nomeGenere;
    public FilmGenereBean(final int idFilm, final String nomeGenere) {
        this.idFilm = idFilm;
        this.nomeGenere = nomeGenere;
    }

    /* =========================================
     * GETTER E SETTER
     * ========================================= */

    //@ ensures \result == idFilm;
    public /*@ pure @*/ int getIdFilm() {
        return idFilm;
    }

    //@ requires idFilm >= 0;
    //@ assigns this.idFilm;
    //@ ensures this.idFilm == idFilm;
    public void setIdFilm(final int idFilm) {
        this.idFilm = idFilm;
    }

    //@ ensures \result == nomeGenere;
    public /*@ pure @*/ String getNomeGenere() {
        return nomeGenere;
    }

    //@ requires nomeGenere != null;
    //@ assigns this.nomeGenere;
    //@ ensures this.nomeGenere == nomeGenere;
    public void setNomeGenere(final String nomeGenere) {
        this.nomeGenere = nomeGenere;
    }
}