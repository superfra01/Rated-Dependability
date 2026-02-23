package model.Entity;

import java.io.Serializable;

public class GenereBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;

    public GenereBean() {
        nome = "";
    }

    public GenereBean(final String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(final String nome) {
        this.nome = nome;
    }
}
