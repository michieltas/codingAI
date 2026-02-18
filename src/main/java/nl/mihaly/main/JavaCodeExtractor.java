package nl.mihaly.main;

import java.util.function.Consumer;

/**
 * Extracts Java source code from AI responses formatted with ```java code blocks.
 *
 * Locates the code fence, extracts the enclosed Java code, and returns it as a string.
 */
public class JavaCodeExtractor {

    private final Consumer<String> logger;

    public JavaCodeExtractor(Consumer<String> logger) {
        this.logger = logger;
    }

    public String extract(String aiResponse) {
        if (aiResponse == null) return null;

        int start = aiResponse.indexOf("```java");
        if (start == -1) return null;

        int end = aiResponse.indexOf("```", start + 7);
        if (end == -1) return null;

        String code = aiResponse.substring(start + 7, end).trim();
        return code.isBlank() ? null : code;
    }

    public String extractXml(String aiResponse) {
        int start = aiResponse.indexOf("```xml");
        if (start == -1) return null;

        start += "```xml".length();
        int end = aiResponse.indexOf("```", start);
        if (end == -1) return null;

        return aiResponse.substring(start, end).trim();
    }

    public String extractDependencies(String aiResponse) {
        int start = aiResponse.indexOf("```xml");
        if (start == -1) return null;

        start += "```xml".length();
        int end = aiResponse.indexOf("```", start);
        if (end == -1) return null;

        return aiResponse.substring(start, end).trim();
    }


}
