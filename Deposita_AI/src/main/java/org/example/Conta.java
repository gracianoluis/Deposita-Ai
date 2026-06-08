package org.example;

public class Conta {
    Long id;
    Double saldo_corrente;

    public Conta(Long id, Double saldo_corrente) {
        this.id = id;
        this.saldo_corrente = saldo_corrente;
    }

    public Long getId() {
        return id;
    }
    public Conta setId(Long id) {
        this.id = id; return this;
    }

    public Double getSaldo_corrente() {
        return saldo_corrente;
    }
    public Conta setSaldo_corrente(Double saldo_corrente) {
        this.saldo_corrente = saldo_corrente; return this;
    }
}
