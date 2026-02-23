package model.Entity;

import java.io.Serializable;

public class ValutazioneBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean likeDislike;
    private String email;
    private String emailRecensore;
    private int idFilm;

    public ValutazioneBean() {
        likeDislike = false;
        email = "";
        emailRecensore = "";
        idFilm = 0;
    }

    public ValutazioneBean(final boolean likeDislike, final String email, final String emailRecensore, final int idFilm) {
        this.likeDislike = likeDislike;
        this.email = email;
        this.emailRecensore = emailRecensore;
        this.idFilm = idFilm;
    }

    public boolean isLikeDislike() {
        return likeDislike;
    }

    public void setLikeDislike(final boolean likeDislike) {
        this.likeDislike = likeDislike;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getEmailRecensore() {
        return emailRecensore;
    }

    public void setEmailRecensore(final String emailRecensore) {
        this.emailRecensore = emailRecensore;
    }

    public int getIdFilm() {
        return idFilm;
    }

    public void setIdFilm(final int idFilm) {
        this.idFilm = idFilm;
    }
}