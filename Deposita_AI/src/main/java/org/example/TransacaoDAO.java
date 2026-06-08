package org.example;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {

    public void registrar(Long conta_id, Long destino_conta_id, String tipo, Double valor) {
        String sql = "INSERT INTO transacoes (conta_id, destino_conta_id, tipo, valor, data_hora) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, conta_id);
            if (destino_conta_id != null) ps.setLong(2, destino_conta_id);
            else ps.setNull(2, Types.BIGINT);
            ps.setString(3, tipo);
            ps.setDouble(4, valor);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar transação.", e);
        }
    }

    public List<Transacao> buscarPorConta(Long conta_id) {
        String sql = """
            SELECT id, conta_id, destino_conta_id, tipo, valor, data_hora
            FROM transacoes
            WHERE conta_id = ? OR destino_conta_id = ?
            ORDER BY data_hora DESC
            """;
        List<Transacao> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, conta_id);
            ps.setLong(2, conta_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long destino = rs.getObject("destino_conta_id") != null
                            ? rs.getLong("destino_conta_id") : null;
                    lista.add(new Transacao(
                            rs.getLong("id"),
                            rs.getLong("conta_id"),
                            destino,
                            rs.getString("tipo"),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data_hora").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar extrato.", e);
        }
        return lista;
    }
}