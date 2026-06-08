package org.example;

import java.time.LocalDateTime;

public class Transacao {
    Long id;
    Long conta_id;
    Long destino_conta_id;
    String tipo;
    Double valor;
    LocalDateTime data_hora;

    public Transacao(Long id, Long conta_id, Long destino_conta_id, String tipo, Double valor, LocalDateTime data_hora) {
        this.id = id;
        this.conta_id = conta_id;
        this.destino_conta_id = destino_conta_id;
        this.tipo = tipo;
        this.valor = valor;
        this.data_hora = data_hora;
    }

    public Long id() {
        return id;
    }
    public Long conta_id() {
        return conta_id;
    }
    public Long destino_conta_id() {
        return destino_conta_id;
    }
    public String tipo() {
        return tipo;
    }
    public Double valor() {
        return valor;
    }
    public LocalDateTime data_hora() {
        return data_hora;
    }
}