package model.Entity;

import java.io.Serializable;

public class ValutazioneBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private boolean likeDislike;
    
    //@ spec_public
    private String email;
    
    //@ spec_public
    private String emailRecensore;
    
    //@ spec_public
    private int idFilm;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant email != null;
    //@ public invariant emailRecensore != null;
    //@ public invariant idFilm >= 0;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.likeDislike == false;
    //@ ensures this.email.equals("");
    //@ ensures this.emailRecensore.equals("");
    //@ ensures this.idFilm == 0;
    public ValutazioneBean() {
        likeDislike = false;
        email = "";
        emailRecensore = "";
        idFilm = 0;
    }

    //@ requires email != null;
    //@ requires emailRecensore != null;
    //@ requires idFilm >= 0;
    //@ ensures this.likeDislike == likeDislike;
    //@ ensures this.email == email;
    //@ ensures this.emailRecensore == emailRecensore;
    //@ ensures this.idFilm == idFilm;
    public ValutazioneBean(final boolean likeDislike, final String email, final String emailRecensore, final int idFilm) {
        this.likeDislike = likeDislike;
        this.email = email;
        this.emailRecensore = emailRecensore;
        this.idFilm = idFilm;
    }

    /* =========================================
     * GETTER E SETTER
     * ========================================= */

    //@ ensures \result == likeDislike;
    public /*@ pure @*/ boolean isLikeDislike() {
        return likeDislike;
    }

    //@ assigns this.likeDislike;
    //@ ensures this.likeDislike == likeDislike;
    public void setLikeDislike(final boolean likeDislike) {
        this.likeDislike = likeDislike;
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

    //@ ensures \result == emailRecensore;
    public /*@ pure @*/ String getEmailRecensore() {
        return emailRecensore;
    }

    //@ requires emailRecensore != null;
    //@ assigns this.emailRecensore;
    //@ ensures this.emailRecensore == emailRecensore;
    public void setEmailRecensore(final String emailRecensore) {
        this.emailRecensore = emailRecensore;
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