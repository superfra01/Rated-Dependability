package model.Entity;

import java.io.Serializable;

public class PreferenzaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String nomeGenere;

    public PreferenzaBean() {
        email = "";
        nomeGenere = "";
    }

    public PreferenzaBean(final String email, final String nomeGenere) {
        this.email = email;
        this.nomeGenere = nomeGenere;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getNomeGenere() {
        return nomeGenere;
    }

    public void setNomeGenere(final String nomeGenere) {
        this.nomeGenere = nomeGenere;
    }
}
