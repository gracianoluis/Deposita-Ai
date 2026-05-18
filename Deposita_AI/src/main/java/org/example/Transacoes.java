package org.example;

import java.time.LocalDateTime;

public class Transacoes {
    Long id;
    Long conta_id;
    String tipo;
    Double valor;
    LocalDateTime data_hora;

    public Transacoes(Long id, Long conta_id, String tipo, Double valor, LocalDateTime data_hora) {
        this.id = id;
        this.conta_id = conta_id;
        this.tipo = tipo;
        this.valor = valor;
        this.data_hora = data_hora;
    }

    public Long id() {
        return id;
    }

    public Transacoes setId(Long id) {
        this.id = id;
        return this;
    }

    public Long conta_id() {
        return conta_id;
    }

    public Transacoes setConta_id(Long conta_id) {
        this.conta_id = conta_id;
        return this;
    }

    public String tipo() {
        return tipo;
    }

    public Transacoes setTipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public LocalDateTime data_hora() {
        return data_hora;
    }

    public Transacoes setData_hora(LocalDateTime data_hora) {
        this.data_hora = data_hora;
        return this;
    }

    public Double valor() {
        return valor;
    }

    public Transacoes setValor(Double valor) {
        this.valor = valor;
        return this;
    }
}
