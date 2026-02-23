package model.Entity;

import java.io.Serializable;

public class FilmBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idFilm;
    private byte[] locandina;
    private String nome;
    private int anno;
    private int durata;
    private String regista;
    private String attori;
    private int valutazione;
    private String trama;

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

    public int getIdFilm() {
        return idFilm;
    }

    public void setIdFilm(final int idFilm) {
        this.idFilm = idFilm;
    }

    public byte[] getLocandina() {
        return locandina;
    }

    public void setLocandina(final byte[] locandina) {
        this.locandina = locandina;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(final String nome) {
        this.nome = nome;
    }

    public int getAnno() {
        return anno;
    }

    public void setAnno(final int anno) {
        this.anno = anno;
    }

    public int getDurata() {
        return durata;
    }

    public void setDurata(final int durata) {
        this.durata = durata;
    }

    public String getRegista() {
        return regista;
    }

    public void setRegista(final String regista) {
        this.regista = regista;
    }

    public String getAttori() {
        return attori;
    }

    public void setAttori(final String attori) {
        this.attori = attori;
    }

    public int getValutazione() {
        return valutazione;
    }

    public void setValutazione(int valutazione) {
        this.valutazione = valutazione;
    }
    
    public String getTrama() {
        return trama;
    }

    public void setTrama(String trama) {
        this.trama = trama;
    }

}
