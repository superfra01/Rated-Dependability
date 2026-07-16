package model.Entity;

import java.io.Serializable;

public class VistoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private String email;
    
    //@ spec_public
    private int idFilm;

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
    //@ assignable \nothing;
    public VistoBean() {
        email = "";
        idFilm = 0;
    }

    //@ requires email != null;
    //@ requires idFilm >= 0;
    //@ ensures this.email == email;
    //@ ensures this.idFilm == idFilm;
    //@ assignable \nothing;
    public VistoBean(final String email, final int idFilm) {
        this.email = email;
        this.idFilm = idFilm;
    }

    /* =========================================
     * GETTER E SETTER
     * ========================================= */

    //@ ensures \result == email;
    public /*@ pure @*/ String getEmail() {
        return email;
    }

    //@ requires email != null;
    //@ assignable this.email;
    //@ ensures this.email == email;
    public void setEmail(final String email) {
        this.email = email;
    }

    //@ ensures \result == idFilm;
    public /*@ pure @*/ int getIdFilm() {
        return idFilm;
    }

    //@ requires idFilm >= 0;
    //@ assignable this.idFilm;
    //@ ensures this.idFilm == idFilm;
    public void setIdFilm(final int idFilm) {
        this.idFilm = idFilm;
    }

}
