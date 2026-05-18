package org.example;

public class Usuarios {
    Long id;
    Long nome;
    int numero_conta;

    public Long id() {
        return id;
    }

    public Usuarios setId(Long id) {
        this.id = id;
        return this;
    }

    public Long nome() {
        return nome;
    }

    public Usuarios setNome(Long nome) {
        this.nome = nome;
        return this;
    }

    public int numero_conta() {
        return numero_conta;
    }

    public Usuarios setNumero_conta(int numero_conta) {
        this.numero_conta = numero_conta;
        return this;
    }

    public Usuarios(Long nome, int numero_conta, Long id) {
        this.nome = nome;
        this.numero_conta = numero_conta;
        this.id = id;

    }
}
