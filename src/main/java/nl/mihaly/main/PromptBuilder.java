package nl.mihaly.main;

public class PromptBuilder {

    public String buildMainPrompt(String className,
                                  String packageName,
                                  String specification,
                                  String testSource,
                                  String testOutput) {

        return Texts.PROMPT.formatted(
                className,
                packageName == null ? "" : packageName,
                specification,
                testSource,
                testOutput
        );
    }

    public String buildFallbackPrompt(String className,
                                      String packageName,
                                      String specification,
                                      String testSource,
                                      String testOutput) {

        return Texts.FALLBACKPROMPT.formatted(
                className,
                packageName == null ? "" : packageName,
                specification,
                testSource,
                testOutput
        );
    }

    public String buildPomPrompt(String testOutput) {
        return Texts.POM_PROMPT.formatted(testOutput);
    }
}
