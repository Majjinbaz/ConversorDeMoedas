import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.Instant;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Conversor {
    private static final String API_KEY = "dc0d47290274573c24f2cf5b"; // Insira sua chave da API
    private static final String LOG_FILE  = "logs.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {
        String[] moedas = {"USD", "EUR", "GBP", "ARS", "AOA", "JPY"};
        Scanner sc = new Scanner(System.in);

        // Mostrar moedas disponíveis
        System.out.println("Moedas disponíveis para conversão:");
        for (String moeda : moedas) {
            System.out.println("- " + moeda);
        }

        // Ler valor em BRL
        System.out.print("\nDigite o valor em BRL: ");
        double valorBRL = sc.nextDouble();
        sc.nextLine(); // consumir quebra de linha

        // Ler moeda de destino
        System.out.print("Digite o código da moeda de destino (ex: USD): ");
        String moedaDestino = sc.nextLine().toUpperCase();

        // Verifica se a moeda é válida
        boolean moedaValida = false;
        for (String m : moedas) {
            if (m.equals(moedaDestino)) {
                moedaValida = true;
                break;
            }
        }

        if (!moedaValida) {
            System.out.println("Moeda inválida. Encerrando programa.");
            sc.close();
            return;
        }

        sc.close();

        HttpClient client = HttpClient.newHttpClient();

        try (PrintWriter logOut = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String url = String.format(
                    "https://v6.exchangerate-api.com/v6/%s/pair/BRL/%s",
                    API_KEY, moedaDestino
            );
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            String responseJson = client
                    .send(req, HttpResponse.BodyHandlers.ofString())
                    .body();

            JsonObject apiObj = JsonParser.parseString(responseJson).getAsJsonObject();

            if (!apiObj.has("conversion_rate")) {
                System.out.println("Erro ao obter taxa de câmbio. Verifique a chave da API.");
                return;
            }

            double taxa = apiObj.get("conversion_rate").getAsDouble();
            double convertido = valorBRL * taxa;

            System.out.printf("→ %.2f BRL = %.2f %s%n",
                    valorBRL, convertido, moedaDestino);

            JsonObject logEntry = new JsonObject();
            logEntry.addProperty("timestamp", Instant.now().toString());
            logEntry.addProperty("from", "BRL");
            logEntry.addProperty("to", moedaDestino);
            logEntry.addProperty("rate", taxa);
            logEntry.addProperty("input", valorBRL);
            logEntry.addProperty("output", convertido);

            logOut.println(gson.toJson(logEntry));

        } catch (IOException e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}
