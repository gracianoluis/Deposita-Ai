package org.example;

public class Conta {
    Long id;
    Long usuario_id;
    Double saldo_corrente;
    Double saldo_poupanca;

    public Conta(Long id, Long usuario_id, Double saldo_corrente, Double saldo_poupanca) {
        this.id = id;
        this.usuario_id = usuario_id;
        this.saldo_corrente = saldo_corrente;
        this.saldo_poupanca = saldo_poupanca;
    }

    public Long id() {
        return id;
    }

    public Conta setId(Long id) {
        this.id = id;
        return this;
    }

    public Long usuario_id() {
        return usuario_id;
    }

    public Conta setUsuario_id(Long usuario_id) {
        this.usuario_id = usuario_id;
        return this;
    }

    public Double saldo_poupanca() {
        return saldo_poupanca;
    }

    public Conta setSaldo_poupanca(Double saldo_poupanca) {
        this.saldo_poupanca = saldo_poupanca;
        return this;
    }

    public Double saldo_corrente() {
        return saldo_corrente;
    }

    public Conta setSaldo_corrente(Double saldo_corrente) {
        this.saldo_corrente = saldo_corrente;
        return this;
    }
}
