package model.Entity;

import java.io.Serializable;

public class GenereBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private String nome;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant nome != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.nome.equals("");
    public GenereBean() {
        nome = "";
    }

    //@ requires nome != null;
    //@ ensures this.nome == nome;
    public GenereBean(final String nome) {
        this.nome = nome;
    }

    /* =========================================
     * GETTER E SETTER
     * ========================================= */

    //@ ensures \result == nome;
    public /*@ pure @*/ String getNome() {
        return nome;
    }

    //@ requires nome != null;
    //@ assigns this.nome;
    //@ ensures this.nome == nome;
    public void setNome(final String nome) {
        this.nome = nome;
    }
}