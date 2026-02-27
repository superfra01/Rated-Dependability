package model.Entity;

import java.io.Serializable;

public class FilmBean implements Serializable {

    private static final long serialVersionUID = 1L;

    //@ spec_public
    private int idFilm;
    
    //@ spec_public
    private byte[] locandina;
    
    //@ spec_public
    private String nome;
    
    //@ spec_public
    private int anno;
    
    //@ spec_public
    private int durata;
    
    //@ spec_public
    private String regista;
    
    //@ spec_public
    private String attori;
    
    //@ spec_public
    private int valutazione;
    
    //@ spec_public
    private String trama;

    /* =========================================
     * INVARIANTI DI CLASSE
     * ========================================= */
    //@ public invariant idFilm >= 0;
    //@ public invariant nome != null;
    //@ public invariant anno >= 0;
    //@ public invariant durata >= 0;
    //@ public invariant regista != null;
    //@ public invariant attori != null;
    //@ public invariant valutazione >= 0;
    //@ public invariant trama != null;

    /* =========================================
     * COSTRUTTORI
     * ========================================= */

    //@ ensures this.idFilm == 0;
    //@ ensures this.locandina == null;
    //@ ensures this.nome.equals("");
    //@ ensures this.anno == 0;
    //@ ensures this.durata == 0;
    //@ ensures this.regista.equals("");
    //@ ensures this.attori.equals("");
    //@ ensures this.valutazione == 1;
    //@ ensures this.trama.equals("");
    public FilmBean() {
        idFilm = 0;
        locandina = null;
        nome = "";
        anno = 0;
        durata = 0;
        regista = "";
        attori = "";
        valutazione = 1;
        trama = "";
    }

    //@ requires idFilm >= 0;
    //@ requires nome != null;
    //@ requires anno >= 0;
    //@ requires durata >= 0;
    //@ requires regista != null;
    //@ requires attori != null;
    //@ requires trama != null;
    //@ ensures this.idFilm == idFilm;
    //@ ensures this.locandina == locandina;
    //@ ensures this.nome == nome;
    //@ ensures this.anno == anno;
    //@ ensures this.durata == durata;
    //@ ensures this.regista == regista;
    //@ ensures this.attori == attori;
    //@ ensures this.valutazione == 1;
    //@ ensures this.trama == trama;
    public FilmBean(final int idFilm,
                    final byte[] locandina,
                    final String nome,
                    final int anno,
                    final int durata,
                    final String regista,
                    final String attori,
                    final String trama) {
        this.idFilm = idFilm;
        this.locandina = locandina;
        this.nome = nome;
        this.anno = anno;
        this.durata = durata;
        this.regista = regista;
        this.attori = attori;
        this.valutazione = 1;
        this.trama = trama;
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

    //@ ensures \result == locandina;
    public /*@ pure @*/ byte[] getLocandina() {
        return locandina;
    }

    //@ assigns this.locandina;
    //@ ensures this.locandina == locandina;
    public void setLocandina(final byte[] locandina) {
        this.locandina = locandina;
    }

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

    //@ ensures \result == anno;
    public /*@ pure @*/ int getAnno() {
        return anno;
    }

    //@ requires anno >= 0;
    //@ assigns this.anno;
    //@ ensures this.anno == anno;
    public void setAnno(final int anno) {
        this.anno = anno;
    }

    //@ ensures \result == durata;
    public /*@ pure @*/ int getDurata() {
        return durata;
    }

    //@ requires durata >= 0;
    //@ assigns this.durata;
    //@ ensures this.durata == durata;
    public void setDurata(final int durata) {
        this.durata = durata;
    }

    //@ ensures \result == regista;
    public /*@ pure @*/ String getRegista() {
        return regista;
    }

    //@ requires regista != null;
    //@ assigns this.regista;
    //@ ensures this.regista == regista;
    public void setRegista(final String regista) {
        this.regista = regista;
    }

    //@ ensures \result == attori;
    public /*@ pure @*/ String getAttori() {
        return attori;
    }

    //@ requires attori != null;
    //@ assigns this.attori;
    //@ ensures this.attori == attori;
    public void setAttori(final String attori) {
        this.attori = attori;
    }

    //@ ensures \result == valutazione;
    public /*@ pure @*/ int getValutazione() {
        return valutazione;
    }

    //@ requires valutazione >= 0;
    //@ assigns this.valutazione;
    //@ ensures this.valutazione == valutazione;
    public void setValutazione(int valutazione) {
        this.valutazione = valutazione;
    }
    
    //@ ensures \result == trama;
    public /*@ pure @*/ String getTrama() {
        return trama;
    }

    //@ requires trama != null;
    //@ assigns this.trama;
    //@ ensures this.trama == trama;
    public void setTrama(String trama) {
        this.trama = trama;
    }
}