package model.Entity;

import java.io.Serializable;

public class ReportBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String emailRecensore;
    private int idFilm;

    public ReportBean() {
        email = "";
        emailRecensore = "";
        idFilm = 0;
    }

    public ReportBean(final String email, final String emailRecensore, final int idFilm) {
        this.email = email;
        this.emailRecensore = emailRecensore;
        this.idFilm = idFilm;
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