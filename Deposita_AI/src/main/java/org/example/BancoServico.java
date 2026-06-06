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
        return String.format("💰 Saldo atual: R$ %.2f", saldo);
    }

    public String depositar(Long conta_id, Double valor) {
        if (valor <= 0) return "❌ Valor inválido para depósito.";

        double novoSaldo = contaDAO.depositar(conta_id, valor);
        transacaoDAO.registrar(conta_id, null, "DEPOSITO", valor);

        return String.format("✅ Depósito de R$ %.2f realizado! Novo saldo: R$ %.2f", valor, novoSaldo);
    }

    public String realizarPix(Long conta_origem_id, Long conta_destino_id, Double valor) {
        if (conta_destino_id == null) return "❌ Informe a conta de destino para o Pix.";
        if (valor <= 0)               return "❌ Valor inválido para o Pix.";

        contaDAO.transferir(conta_origem_id, conta_destino_id, valor);
        transacaoDAO.registrar(conta_origem_id, conta_destino_id, "PIX", valor);

        return String.format("✅ Pix de R$ %.2f enviado para conta %d!", valor, conta_destino_id);
    }

    public String consultarExtrato(Long conta_id) {
        List<Transacao> lista = transacaoDAO.buscarPorConta(conta_id);

        if (lista.isEmpty()) return "📄 Nenhuma movimentação encontrada.";

        StringBuilder sb = new StringBuilder("📄 Extrato:\n");
        for (Transacao t : lista) {
            sb.append(String.format("  • [%s] %s — R$ %.2f%n",
                    t.data_hora().toLocalDate(), t.tipo(), t.valor()));
        }
        return sb.toString();
    }
}