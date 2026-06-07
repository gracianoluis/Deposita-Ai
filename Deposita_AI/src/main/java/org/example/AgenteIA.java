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
        Você é um assistente bancário. Analise a mensagem do usuário e responda SOMENTE com um JSON válido, sem explicações.
        
        Formato obrigatório:
        {
          "intencao": "SALDO" | "DEPOSITO" | "PIX" | "EXTRATO" | "DESCONHECIDO",
          "valor": 0.0,
          "destino_conta_id": null,
          "mensagem": "resposta amigável ao usuário"
        }
        
        Exemplos:
        - "qual meu saldo?" → {"intencao":"SALDO","valor":0,"destino_conta_id":null,"mensagem":"Consultando seu saldo..."}
        - "depositar 150 reais" → {"intencao":"DEPOSITO","valor":150.0,"destino_conta_id":null,"mensagem":"Realizando depósito de R$ 150,00..."}
        - "pix 200 para conta 3" → {"intencao":"PIX","valor":200.0,"destino_conta_id":3,"mensagem":"Realizando Pix..."}
        - "ver extrato" → {"intencao":"EXTRATO","valor":0,"destino_conta_id":null,"mensagem":"Buscando seu extrato..."}
        - "criar conta" → {"intencao":"NOVA_CONTA","valor":0,"destino_conta_id":null,"mensagem":"Criando nova conta..."}
        """;

    public AgenteIA(Ollama ollama, BancoServico servico) {
        this.ollama = ollama;
        this.servico = servico;
    }

    public String processar(Long contaId, String mensagemUsuario) {
        try {
            OllamaChatRequest request = OllamaChatRequest.builder()
                    .withModel(model)
                    .withMessage(OllamaChatMessageRole.SYSTEM, SYSTEM_PROMPT)
                    .withMessage(OllamaChatMessageRole.USER, mensagemUsuario)
                    .build();

            OllamaChatResult result = ollama.chat(request, null);
            String jsonResposta = result.getResponseModel().getMessage().getResponse();

            return executarIntencao(contaId, jsonResposta);

        } catch (Exception e) {
            return "Erro ao processar: " + e.getMessage();
        }
    }

    private String executarIntencao(Long contaId, String json) throws Exception {
        json = json.replaceAll("```json|```", "").trim();

        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> dados = mapper.readValue(json, Map.class);

        String intencao   = (String) dados.get("intencao");
        String mensagem   = (String) dados.get("mensagem");
        double valor      = dados.get("valor") != null ? ((Number) dados.get("valor")).doubleValue() : 0;
        Object destinoObj = dados.get("destino_conta_id");
        Long destinoId    = destinoObj != null ? ((Number) destinoObj).longValue() : null;

        return switch (intencao) {
            case "SALDO"    -> mensagem + "\n" + servico.consultarSaldo(contaId);
            case "DEPOSITO" -> mensagem + "\n" + servico.depositar(contaId, valor);
            case "PIX"      -> mensagem + "\n" + servico.realizarPix(contaId, destinoId, valor);
            case "EXTRATO"  -> mensagem + "\n" + servico.consultarExtrato(contaId);
            default         -> "Não entendi. Tente: saldo, depositar, pix ou extrato.";
        };
    }

}