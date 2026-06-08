package org.example;

import java.util.List;

public class BancoServico {

    private final ContaDAO contaDAO;
    private final TransacaoDAO transacaoDAO;

    public BancoServico(ContaDAO contaDAO, TransacaoDAO transacaoDAO) {
        this.contaDAO = contaDAO;
        this.transacaoDAO = transacaoDAO;
    }

    public String consultarSaldo(Long conta_id) {
        double saldo = contaDAO.buscarSaldo(conta_id);
        return String.format("💰 Saldo da conta %d: R$ %.2f", conta_id, saldo);
    }

    public String listarContas() {
        List<Conta> contas = contaDAO.listarContas();
        if (contas.isEmpty()) return "Nenhuma conta cadastrada.";
        StringBuilder sb = new StringBuilder("🏦 Contas cadastradas:\n");
        for (Conta c : contas) {
            sb.append(String.format("  • Conta %d — R$ %.2f%n", c.getId(), c.getSaldo_corrente()));
        }
        return sb.toString();
    }

    public String criarConta() {
        Long novaId = contaDAO.criarConta();
        return String.format("✅ Conta %d criada com saldo R$ 0,00!", novaId);
    }

    public String depositar(Long conta_id, Double valor) {
        if (valor <= 0) return "❌ Valor inválido.";
        double novoSaldo = contaDAO.depositar(conta_id, valor);
        transacaoDAO.registrar(conta_id, null, "DEPOSITO", valor);
        return String.format("✅ Depósito de R$ %.2f na conta %d. Novo saldo: R$ %.2f",
                valor, conta_id, novoSaldo);
    }

    public String realizarPix(Long origem, Long destino, Double valor) {
        if (destino == null) return "❌ Informe a conta de destino.";
        if (valor <= 0)      return "❌ Valor inválido.";
        contaDAO.transferir(origem, destino, valor);
        transacaoDAO.registrar(origem, destino, "PIX", valor);
        return String.format("✅ Pix de R$ %.2f da conta %d para conta %d realizado!",
                valor, origem, destino);
    }

    public String consultarExtrato(Long conta_id) {
        List<Transacao> lista = transacaoDAO.buscarPorConta(conta_id);
        if (lista.isEmpty()) return "📄 Nenhuma movimentação encontrada.";
        StringBuilder sb = new StringBuilder("📄 Extrato da conta " + conta_id + ":\n");
        for (Transacao t : lista) {
            if (t.tipo().equals("PIX")) {
                String direcao = t.conta_id().equals(conta_id) ? "→ enviado para conta " + t.destino_conta_id()
                        : "← recebido da conta " + t.conta_id();
                sb.append(String.format("  • [%s] PIX R$ %.2f %s%n",
                        t.data_hora().toLocalDate(), t.valor(), direcao));
            } else {
                sb.append(String.format("  • [%s] %s R$ %.2f%n",
                        t.data_hora().toLocalDate(), t.tipo(), t.valor()));
            }
        }
        return sb.toString();
    }
}