package org.example;

import io.github.ollama4j.Ollama;
import java.util.Scanner;

public class App {
  public static void main(String[] args) throws Exception {

    Ollama ollama = new Ollama("http://localhost:11434/");
    ollama.setRequestTimeoutSeconds(300);

    Long contaId = 1L;

    ContaDAO contaDAO         = new ContaDAO();
    TransacaoDAO transacaoDAO = new TransacaoDAO();
    BancoServico servico      = new BancoServico(contaDAO, transacaoDAO);
    AgenteIA agente           = new AgenteIA(ollama, servico);

    Scanner scanner = new Scanner(System.in);
    System.out.println("🏦 Bem-vindo ao Deposita AI! Como posso ajudar?");
    System.out.println("(Digite 'sair' para encerrar)\n");

    while (true) {
      System.out.print("Você: ");
      String entrada = scanner.nextLine().trim();
      if (entrada.equalsIgnoreCase("sair")) break;

      String resposta = agente.processar(contaId, entrada);
      System.out.println("Agente: " + resposta + "\n");
    }

    System.out.println("Até logo!");
  }
}