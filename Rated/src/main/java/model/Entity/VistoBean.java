package model.Entity;

import java.io.Serializable;

public class VistoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private int idFilm;

    public VistoBean() {
        email = "";
        idFilm = 0;
    }

    public VistoBean(final String email, final int idFilm) {
        this.email = email;
        this.idFilm = idFilm;
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
}
