package model.Entity;

import java.io.Serializable;

public class UtenteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private String email;
    
    //@ spec_public
    private byte /*@ nullable @*/ [] icona;
    
    //@ spec_public
    private String username;
    
    //@ spec_public
    private String password;
    
    //@ spec_public
    private String tipoUtente;
    
    //@ spec_public
    private int nWarning;
    
    //@ spec_public
    private String biografia;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant email != null;
    //@ public invariant username != null;
    //@ public invariant password != null;
    //@ public invariant tipoUtente != null;
    //@ public invariant biografia != null;
    //@ public invariant nWarning >= 0;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.email.equals("");
    //@ ensures this.icona == null;
    //@ ensures this.username.equals("");
    //@ ensures this.password.equals("");
    //@ ensures this.tipoUtente.equals("");
    //@ ensures this.nWarning == 0;
    //@ ensures this.biografia.equals("");
    //@ assignable \nothing;
    public UtenteBean() {
        email = "";
        icona = null;
        username = "";
        password = "";
        tipoUtente = "";
        nWarning = 0;
        biografia = "";
    }

    //@ requires email != null;
    //@ requires username != null;
    //@ requires password != null;
    //@ requires tipoUtente != null;
    //@ requires biografia != null;
    //@ requires nWarning >= 0;
    //@ ensures this.email == email;
    //@ ensures this.icona == icona;
    //@ ensures this.username == username;
    //@ ensures this.password == password;
    //@ ensures this.tipoUtente == tipoUtente;
    //@ ensures this.nWarning == nWarning;
    //@ ensures this.biografia == biografia;
    //@ assignable \nothing;
    public UtenteBean(final String email, final byte /*@ nullable @*/ [] icona, final String username, final String password, final String tipoUtente, final int nWarning, final String biografia) {
        this.email = email;
        this.icona = icona;
        this.username = username;
        this.password = password;
        this.tipoUtente = tipoUtente;
        this.nWarning = nWarning;
        this.biografia = biografia;
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

    //@ ensures \result == icona;
    public /*@ pure @*/ byte /*@ nullable @*/ [] getIcona() {
        return icona;
    }

    //@ assignable this.icona;
    //@ ensures this.icona == icona;
    public void setIcona(final byte /*@ nullable @*/ [] icona) {
        this.icona = icona;
    }

    //@ ensures \result == username;
    public /*@ pure @*/ String getUsername() {
        return username;
    }

    //@ requires username != null;
    //@ assignable this.username;
    //@ ensures this.username == username;
    public void setUsername(final String username) {
        this.username = username;
    }

    //@ ensures \result == password;
    public /*@ pure @*/ String getPassword() {
        return password;
    }

    //@ requires password != null;
    //@ assignable this.password;
    //@ ensures this.password == password;
    public void setPassword(final String password) {
        this.password = password;
    }

    //@ ensures \result == tipoUtente;
    public /*@ pure @*/ String getTipoUtente() {
        return tipoUtente;
    }

    //@ requires tipoUtente != null;
    //@ assignable this.tipoUtente;
    //@ ensures this.tipoUtente == tipoUtente;
    public void setTipoUtente(final String tipoUtente) {
        this.tipoUtente = tipoUtente;
    }

    //@ ensures \result == nWarning;
    public /*@ pure @*/ int getNWarning() {
        return nWarning;
    }

    //@ requires nWarning >= 0;
    //@ assignable this.nWarning;
    //@ ensures this.nWarning == nWarning;
    public void setNWarning(final int nWarning) {
        this.nWarning = nWarning;
    }
    
    //@ ensures \result == biografia;
    public /*@ pure @*/ String getBiografia() {
        return biografia;
    }

    //@ requires biografia != null;
    //@ assignable this.biografia;
    //@ ensures this.biografia == biografia;
    public void setBiografia(final String biografia) {
        this.biografia = biografia;
    }

}
