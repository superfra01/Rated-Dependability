package model.Entity;

import java.io.Serializable;

public class RecensioneBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private String titolo;
    //@ spec_public
    private String contenuto;
    //@ spec_public
    private int valutazione;
    //@ spec_public
    private int nLike;
    //@ spec_public
    private int nDislike;
    //@ spec_public
    private int nReports;
    //@ spec_public
    private String email;
    //@ spec_public
    private int idFilm;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant titolo != null;
    //@ public invariant contenuto != null;
    //@ public invariant email != null;
    //@ public invariant valutazione >= 0;
    //@ public invariant nLike >= 0;
    //@ public invariant nDislike >= 0;
    //@ public invariant nReports >= 0;
    //@ public invariant idFilm >= 0;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.titolo.equals("");
    //@ ensures this.contenuto.equals("");
    //@ ensures this.valutazione == 0;
    //@ ensures this.nLike == 0;
    //@ ensures this.nDislike == 0;
    //@ ensures this.nReports == 0;
    //@ ensures this.email.equals("");
    //@ ensures this.idFilm == 0;
    public RecensioneBean() {
        titolo = "";
        contenuto = "";
        valutazione = 0;
        nLike = 0;
        nDislike = 0;
        nReports = 0;
        email = "";
        idFilm = 0;
    }

    //@ requires titolo != null;
    //@ requires contenuto != null;
    //@ requires email != null;
    //@ requires valutazione >= 0;
    //@ requires nLike >= 0;
    //@ requires nDislike >= 0;
    //@ requires nReports >= 0;
    //@ requires idFilm >= 0;
    //@ ensures this.titolo == titolo;
    //@ ensures this.contenuto == contenuto;
    //@ ensures this.valutazione == valutazione;
    //@ ensures this.nLike == nLike;
    //@ ensures this.nDislike == nDislike;
    //@ ensures this.nReports == nReports;
    //@ ensures this.email == email;
    //@ ensures this.idFilm == idFilm;
    public RecensioneBean(final String titolo, final String contenuto, final int valutazione, final int nLike, final int nDislike, final int nReports, final String email, final int idFilm) {
        this.titolo = titolo;
        this.contenuto = contenuto;
        this.valutazione = valutazione;
        this.nLike = nLike;
        this.nDislike = nDislike;
        this.nReports = nReports;
        this.email = email;
        this.idFilm = idFilm;
    }

    /* =========================================
     * GETTER E SETTER
     * ========================================= */

    //@ ensures \result == titolo;
    public /*@ pure @*/ String getTitolo() {
        return titolo;
    }

    //@ requires titolo != null;
    //@ assigns this.titolo;
    //@ ensures this.titolo == titolo;
    public void setTitolo(final String titolo) {
        this.titolo = titolo;
    }

    //@ ensures \result == contenuto;
    public /*@ pure @*/ String getContenuto() {
        return contenuto;
    }

    //@ requires contenuto != null;
    //@ assigns this.contenuto;
    //@ ensures this.contenuto == contenuto;
    public void setContenuto(final String contenuto) {
        this.contenuto = contenuto;
    }

    //@ ensures \result == valutazione;
    public /*@ pure @*/ int getValutazione() {
        return valutazione;
    }

    //@ requires valutazione >= 0;
    //@ assigns this.valutazione;
    //@ ensures this.valutazione == valutazione;
    public void setValutazione(final int valutazione) {
        this.valutazione = valutazione;
    }

    //@ ensures \result == nLike;
    public /*@ pure @*/ int getNLike() {
        return nLike;
    }

    //@ requires nLike >= 0;
    //@ assigns this.nLike;
    //@ ensures this.nLike == nLike;
    public void setNLike(final int nLike) {
        this.nLike = nLike;
    }

    //@ ensures \result == nDislike;
    public /*@ pure @*/ int getNDislike() {
        return nDislike;
    }

    //@ requires nDislike >= 0;
    //@ assigns this.nDislike;
    //@ ensures this.nDislike == nDislike;
    public void setNDislike(final int nDislike) {
        this.nDislike = nDislike;
    }

    //@ ensures \result == nReports;
    public /*@ pure @*/ int getNReports() {
        return nReports;
    }

    //@ requires nReports >= 0;
    //@ assigns this.nReports;
    //@ ensures this.nReports == nReports;
    public void setNReports(final int nReports) {
        this.nReports = nReports;
    }

    //@ ensures \result == email;
    public /*@ pure @*/ String getEmail() {
        return email;
    }

    //@ requires email != null;
    //@ assigns this.email;
    //@ ensures this.email == email;
    public void setEmail(final String email) {
        this.email = email;
    }

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
}