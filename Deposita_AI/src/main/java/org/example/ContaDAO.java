package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContaDAO {

    public double buscarSaldo(Long conta_id) {
        String sql = "SELECT saldo_corrente FROM contas WHERE id = ?";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, conta_id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("saldo_corrente");
                }
            }

            throw new RuntimeException("Conta não encontrada.");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar saldo.", e);
        }
    }

    public Conta buscarPorId(Long conta_id) {
        String sql = "SELECT id, usuario_id, saldo_corrente, saldo_poupanca FROM contas WHERE id = ?";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, conta_id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Conta(
                            rs.getLong("id"),
                            rs.getLong("usuario_id"),
                            rs.getDouble("saldo_corrente"),
                            rs.getDouble("saldo_poupanca")
                    );
                }
            }

            throw new RuntimeException("Conta não encontrada.");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar conta.", e);
        }
    }

    public double depositar(Long conta_id, Double valor) {
        double saldoAtual = buscarSaldo(conta_id);
        double novoSaldo = saldoAtual + valor;

        String sql = "UPDATE contas SET saldo_corrente = ? WHERE id = ?";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, novoSaldo);
            ps.setLong(2, conta_id);

            int linhas = ps.executeUpdate();
            if (linhas == 0) throw new RuntimeException("Conta não encontrada para depósito.");

            return novoSaldo;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao realizar depósito.", e);
        }
    }

    public void transferir(Long conta_origem_id, Long conta_destino_id, Double valor) {
        double saldoOrigem = buscarSaldo(conta_origem_id);

        if (saldoOrigem < valor) {
            throw new RuntimeException("Saldo insuficiente para transferência.");
        }

        double novoSaldoOrigem  = saldoOrigem - valor;
        double saldoDestino     = buscarSaldo(conta_destino_id);
        double novoSaldoDestino = saldoDestino + valor;

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
                throw new RuntimeException("Erro na transferência. Operação revertida.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão na transferência.", e);
        }
    }
}