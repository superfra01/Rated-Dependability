package model.Entity;

import java.io.Serializable;

public class UtenteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private byte[] icona;
    private String username;
    private String password;
    private String tipoUtente;
    private int nWarning;
    private String biografia;

    public UtenteBean() {
        email = "";
        icona = null;
        username = "";
        password = "";
        tipoUtente = "";
        nWarning = 0;
        biografia = "";
    }

    public UtenteBean(final String email, final byte[] icona, final String username, final String password, final String tipoUtente, final int nWarning, final String biografia) {
        this.email = email;
        this.icona = icona;
        this.username = username;
        this.password = password;
        this.tipoUtente = tipoUtente;
        this.nWarning = nWarning;
        this.biografia = biografia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public byte[] getIcona() {
        return icona;
    }

    public void setIcona(final byte[] icona) {
        this.icona = icona;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getTipoUtente() {
        return tipoUtente;
    }

    public void setTipoUtente(final String tipoUtente) {
        this.tipoUtente = tipoUtente;
    }

    public int getNWarning() {
        return nWarning;
    }

    public void setNWarning(final int nWarning) {
        this.nWarning = nWarning;
    }
    
    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(final String biografia) {
        this.biografia = biografia;
    }
}