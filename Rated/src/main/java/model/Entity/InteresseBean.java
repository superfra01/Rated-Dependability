package model.Entity;

import java.io.Serializable;

public class InteresseBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private String email;
    
    //@ spec_public
    private int idFilm;
    
    //@ spec_public
    private boolean interesse;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant email != null;
    //@ public invariant idFilm >= 0;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.email.equals("");
    //@ ensures this.idFilm == 0;
    //@ ensures this.interesse == false;
    public InteresseBean() {
        email = "";
        idFilm = 0;
        interesse = false;
    }

    //@ requires email != null;
    //@ requires idFilm >= 0;
    //@ ensures this.email == email;
    //@ ensures this.idFilm == idFilm;
    //@ ensures this.interesse == interesse;
    public InteresseBean(final String email, final int idFilm, final boolean interesse) {
        this.email = email;
        this.idFilm = idFilm;
        this.interesse = interesse;
    }

    /* =========================================
     * GETTER E SETTER
     * ========================================= */

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

    //@ ensures \result == interesse;
    public /*@ pure @*/ boolean isInteresse() {
        return interesse;
    }

    //@ assigns this.interesse;
    //@ ensures this.interesse == interesse;
    public void setInteresse(final boolean interesse) {
        this.interesse = interesse;
    }
}