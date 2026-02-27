package model.Entity;

import java.io.Serializable;

public class PreferenzaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private String email;
    
    //@ spec_public
    private String nomeGenere;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant email != null;
    //@ public invariant nomeGenere != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.email.equals("");
    //@ ensures this.nomeGenere.equals("");
    public PreferenzaBean() {
        email = "";
        nomeGenere = "";
    }

    //@ requires email != null;
    //@ requires nomeGenere != null;
    //@ ensures this.email == email;
    //@ ensures this.nomeGenere == nomeGenere;
    public PreferenzaBean(final String email, final String nomeGenere) {
        this.email = email;
        this.nomeGenere = nomeGenere;
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