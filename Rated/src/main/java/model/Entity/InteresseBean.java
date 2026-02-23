package model.Entity;

import java.io.Serializable;

public class InteresseBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private int idFilm;
    private boolean interesse;

    public InteresseBean() {
        email = "";
        idFilm = 0;
        interesse = false;
    }

    public InteresseBean(final String email, final int idFilm, final boolean interesse) {
        this.email = email;
        this.idFilm = idFilm;
        this.interesse = interesse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public int getIdFilm() {
        return idFilm;
    }

    public void setIdFilm(final int idFilm) {
        this.idFilm = idFilm;
    }

    public boolean isInteresse() {
        return interesse;
    }

    public void setInteresse(final boolean interesse) {
        this.interesse = interesse;
    }
}
