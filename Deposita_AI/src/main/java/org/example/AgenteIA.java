package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;

import java.util.Map;

public class AgenteIA {

    private final Ollama ollama;
    private final String model = "qwen3.5:2b";
    private final BancoServico servico;

    private static final String SYSTEM_PROMPT = """
    Você é um assistente bancário. Responda APENAS com JSON puro, sem texto antes ou depois, sem markdown.
    
    Formato EXATO (todos os campos obrigatórios):
    {"intencao":"...","valor":0,"origem_conta_id":1,"destino_conta_id":null,"mensagem":"..."}
    
    REGRAS:
    - origem_conta_id: SEMPRE um número quando a operação envolve uma conta. NUNCA null para SALDO, DEPOSITO, PIX, EXTRATO
    - destino_conta_id: apenas para PIX, senão null
    - valor: apenas para DEPOSITO e PIX, senão 0
    
    Valores de intencao:
    - SALDO
    - TODAS_CONTAS
    - DEPOSITO
    - PIX
    - EXTRATO
    - NOVA_CONTA
    - DESCONHECIDO
    
    Exemplos:
    "saldo da conta 1" -> {"intencao":"SALDO","valor":0,"origem_conta_id":1,"destino_conta_id":null,"mensagem":"Consultando saldo da conta 1..."}
    "saldo" -> {"intencao":"SALDO","valor":0,"origem_conta_id":1,"destino_conta_id":null,"mensagem":"Consultando saldo da conta 1..."}
    "listar contas" -> {"intencao":"TODAS_CONTAS","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"Listando contas..."}
    "ver todas as contas" -> {"intencao":"TODAS_CONTAS","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"Listando contas..."}
    "depositar 150 na conta 2" -> {"intencao":"DEPOSITO","valor":150.0,"origem_conta_id":2,"destino_conta_id":null,"mensagem":"Depositando R$ 150,00..."}
    "pix 200 da conta 1 para conta 3" -> {"intencao":"PIX","valor":200.0,"origem_conta_id":1,"destino_conta_id":3,"mensagem":"Enviando Pix..."}
    "extrato da conta 1" -> {"intencao":"EXTRATO","valor":0,"origem_conta_id":1,"destino_conta_id":null,"mensagem":"Buscando extrato..."}
    "criar conta" -> {"intencao":"NOVA_CONTA","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"Criando nova conta..."}
    """;

    public AgenteIA(Ollama ollama, BancoServico servico) {
        this.ollama = ollama;
        this.servico = servico;
    }

    public String processar(String mensagemUsuario) {
        try {
            OllamaChatRequest request = OllamaChatRequest.builder()
                    .withModel(model)
                    .withMessage(OllamaChatMessageRole.SYSTEM, SYSTEM_PROMPT)
                    .withMessage(OllamaChatMessageRole.USER, mensagemUsuario)
                    .build();

            OllamaChatResult result = ollama.chat(request, null);
            String jsonResposta = result.getResponseModel().getMessage().getResponse();

            return executarIntencao(jsonResposta);

        } catch (Exception e) {
            return "Erro ao processar: " + e.getMessage();
        }
    }

    private String executarIntencao(String json) throws Exception {
        int inicio = json.indexOf('{');
        int fim = json.lastIndexOf('}');
        if (inicio == -1 || fim == -1)
            return "Não entendi. Tente: saldo da conta 1, depositar 100 na conta 1, pix 50 da conta 1 para conta 2.";
        json = json.substring(inicio, fim + 1);

        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> dados = mapper.readValue(json, Map.class);

        String intencao   = (String) dados.get("intencao");
        String mensagem   = (String) dados.get("mensagem");
        double valor      = dados.get("valor") != null ? ((Number) dados.get("valor")).doubleValue() : 0;
        Object origemObj  = dados.get("origem_conta_id");
        Object destinoObj = dados.get("destino_conta_id");
        Long origemId     = origemObj  != null ? ((Number) origemObj).longValue()  : null;
        Long destinoId    = destinoObj != null ? ((Number) destinoObj).longValue() : null;

        if (intencao == null) return "Não entendi sua solicitação.";

        return switch (intencao) {
            case "SALDO" -> {
                if (origemId == null) yield "❌ Informe o número da conta. Ex: 'saldo da conta 1'";
                yield mensagem + "\n" + servico.consultarSaldo(origemId);
            }
            case "TODAS_CONTAS" -> mensagem + "\n" + servico.listarContas();
            case "DEPOSITO" -> {
                if (origemId == null) yield "❌ Informe o número da conta. Ex: 'depositar 100 na conta 1'";
                yield mensagem + "\n" + servico.depositar(origemId, valor);
            }
            case "PIX" -> {
                if (origemId == null || destinoId == null)
                    yield "❌ Informe conta origem e destino. Ex: 'pix 50 da conta 1 para conta 2'";
                yield mensagem + "\n" + servico.realizarPix(origemId, destinoId, valor);
            }
            case "EXTRATO" -> {
                if (origemId == null) yield "❌ Informe o número da conta. Ex: 'extrato da conta 1'";
                yield mensagem + "\n" + servico.consultarExtrato(origemId);
            }
            case "NOVA_CONTA" -> mensagem + "\n" + servico.criarConta();
            default -> "Não entendi. Tente: 'saldo da conta 1', 'depositar 100 na conta 1', 'criar conta'.";
        };
    }
}