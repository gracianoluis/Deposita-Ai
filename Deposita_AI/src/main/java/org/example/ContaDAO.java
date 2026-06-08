package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    public double buscarSaldo(Long conta_id) {
        String sql = "SELECT saldo_corrente FROM contas WHERE id = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, conta_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("saldo_corrente");
            }
            throw new RuntimeException("Conta " + conta_id + " não encontrada.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar saldo.", e);
        }
    }

    public List<Conta> listarContas() {
        String sql = "SELECT id, saldo_corrente FROM contas ORDER BY id";
        List<Conta> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Conta(rs.getLong("id"), rs.getDouble("saldo_corrente")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar contas.", e);
        }
        return lista;
    }

    public Long criarConta() {
        String sql = "INSERT INTO contas (saldo_corrente) VALUES (0) RETURNING id";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("id");
            throw new RuntimeException("Erro ao criar conta.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar conta.", e);
        }
    }

    public double depositar(Long conta_id, Double valor) {
        double novoSaldo = buscarSaldo(conta_id) + valor;
        String sql = "UPDATE contas SET saldo_corrente = ? WHERE id = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, novoSaldo);
            ps.setLong(2, conta_id);
            if (ps.executeUpdate() == 0) throw new RuntimeException("Conta não encontrada.");
            return novoSaldo;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao depositar.", e);
        }
    }

    public void transferir(Long conta_origem_id, Long conta_destino_id, Double valor) {
        double saldoOrigem = buscarSaldo(conta_origem_id);
        if (saldoOrigem < valor) throw new RuntimeException("Saldo insuficiente.");

        double novoSaldoOrigem  = saldoOrigem - valor;
        double novoSaldoDestino = buscarSaldo(conta_destino_id) + valor;

        String sql = "UPDATE contas SET saldo_corrente = ? WHERE id = ?";
        try (Connection con = ConexaoBD.conectar()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, novoSaldoOrigem);
                ps.setLong(2, conta_origem_id);
                ps.executeUpdate();

                ps.setDouble(1, novoSaldoDestino);
                ps.setLong(2, conta_destino_id);
                ps.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException("Erro na transferência. Revertido.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão.", e);
        }
    }
}