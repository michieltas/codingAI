package nl.mihaly.main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Sends prompts to the local Ollama server and returns model responses.
 *
 * Handles HTTP communication, JSON construction, and decoding of escaped content
 * so that Java and XML (pom.xml) arrive in a clean, usable form.
 */
public class OllamaClient {

    private final Consumer<String> logger;

    public OllamaClient(Consumer<String> logger) {
        this.logger = logger;
    }

    public String call(String model, String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String safePrompt = jsonEscape(prompt);

            String json = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "stream": false
                }
                """.formatted(model, safePrompt);

            logger.accept("Sending to Ollama:");
            logger.accept(json);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            logger.accept("Raw Ollama response:");
            logger.accept(resp.body());

            String decoded = decodeAllEscapes(resp.body());

            logger.accept("Decoded Ollama response:");
            logger.accept(decoded);

            return decoded;

        } catch (Exception e) {
            String msg = "Error calling Ollama: " + e.getMessage();
            logger.accept(msg);
            return msg;
        }
    }

    /**
     * Escapes a Java string so it becomes safe to embed inside a JSON string literal.
     */
    private String jsonEscape(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Decodes JSON, Unicode (\\uXXXX) and basic HTML/XML escapes
     * so that generated Java and XML are clean.
     */
    private String decodeAllEscapes(String input) {
        // 1. JSON-style escapes
        String s = input
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\""); // <-- deze regel toevoegen

        // 2. Unicode escapes (\\uXXXX)
        s = decodeUnicodeEscapes(s);

        // 3. HTML/XML escapes
        s = s.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");

        return s;
    }

    private String decodeUnicodeEscapes(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (i + 5 < input.length()
                    && input.charAt(i) == '\\'
                    && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    sb.append((char) code);
                    i += 5;
                    continue;
                } catch (NumberFormatException ignored) {
                    // fall through and append literally
                }
            }
            sb.append(input.charAt(i));
        }
        return sb.toString();
    }
}
