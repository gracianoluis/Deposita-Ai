package org.example;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;

public class App {

  public static void main(String[] args) throws OllamaException {

    Ollama ollama = new Ollama("http://localhost:11434/");

    ollama.setRequestTimeoutSeconds(300);

    String model = "qwen2.5-coder:7b";

    String pergunta = "O que é Java?";

    OllamaChatRequest request = OllamaChatRequest.builder()
            .withModel(model)
            .withMessage(OllamaChatMessageRole.USER, pergunta)
            .build();

    OllamaChatResult result = ollama.chat(request, null);

    System.out.println(pergunta);

    System.out.println(result.getResponseModel().getMessage().getResponse());

  }
}