package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgenteIA {

    private final Ollama ollama;
    private final String model = "qwen3.5:2b";
    private final BancoServico servico;

    private static final String SYSTEM_PROMPT = """
        Você é um assistente bancário. Responda APENAS com JSON puro, sem texto antes ou depois, sem markdown, sem tags <think>.
        
        Formato EXATO:
        {"intencao":"...","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"..."}
        
        Valores de intencao:
        - SALDO: consultar saldo (origem_conta_id obrigatorio)
        - TODAS_CONTAS: listar todas as contas
        - DEPOSITO: depositar (origem_conta_id e valor obrigatorios)
        - PIX: transferir (origem_conta_id, destino_conta_id e valor obrigatorios)
        - EXTRATO: historico (origem_conta_id obrigatorio)
        - NOVA_CONTA: criar conta
        - DESCONHECIDO: outros
        
        Exemplos de casos:
        usuario diz "saldo da conta 1" ou "saldo conta 1":
        {"intencao":"SALDO","valor":0,"origem_conta_id":1,"destino_conta_id":null,"mensagem":"Consultando saldo da conta 1..."}
        
        usuario diz "listar contas":
        {"intencao":"TODAS_CONTAS","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"Listando contas..."}
        
        usuario diz "contas cadastradas":
        {"intencao":"TODAS_CONTAS","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"Listando contas..."}
        
        usuario diz "depositar 150 na conta 2":
        {"intencao":"DEPOSITO","valor":150.0,"origem_conta_id":2,"destino_conta_id":null,"mensagem":"Depositando R$ 150,00 na conta 2..."}
        
        usuario diz "pix 200 da conta 1 para conta 3":
        {"intencao":"PIX","valor":200.0,"origem_conta_id":1,"destino_conta_id":3,"mensagem":"Enviando Pix de R$ 200,00..."}
        
        usuario diz "extrato da conta 1":
        {"intencao":"EXTRATO","valor":0,"origem_conta_id":1,"destino_conta_id":null,"mensagem":"Buscando extrato da conta 1..."}
        
        usuario diz "criar conta":
        {"intencao":"NOVA_CONTA","valor":0,"origem_conta_id":null,"destino_conta_id":null,"mensagem":"Criando nova conta..."}
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
                    .withMessage(OllamaChatMessageRole.USER, mensagemUsuario + " /no_think")
                    .build();

            OllamaChatResult result = ollama.chat(request, null);
            String jsonResposta = result.getResponseModel().getMessage().getResponse();

            return executarIntencao(jsonResposta);

        } catch (Exception e) {
            return "Erro ao processar: " + e.getMessage();
        }
    }

    private String extrairJson(String texto) {
        // Remove bloco <think>...</think> se existir
        texto = texto.replaceAll("(?s)<think>.*?</think>", "").trim();

        // Extrai o JSON com regex — pega o último {} válido
        Pattern pattern = Pattern.compile("\\{[^{}]*\\}");
        Matcher matcher = pattern.matcher(texto);
        String ultimo = null;
        while (matcher.find()) {
            ultimo = matcher.group();
        }
        return ultimo;
    }

    private String executarIntencao(String textoResposta) throws Exception {
        String json = extrairJson(textoResposta);

        if (json == null)
            return "Não entendi. Tente: 'saldo da conta 1', 'depositar 100 na conta 1', 'listar contas', 'criar conta'.";

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
            default -> "Não entendi. Tente: 'saldo da conta 1', 'depositar 100 na conta 1', 'listar contas', 'criar conta'.";
        };
    }
}