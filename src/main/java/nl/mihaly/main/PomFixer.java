package nl.mihaly.main;

import java.nio.file.Path;
import java.util.function.Consumer;

public class PomFixer {

    private final Consumer<String> logger;
    private final JavaCodeExtractor extractor;
    private final PromptBuilder prompts;

    public PomFixer(Consumer<String> logger, JavaCodeExtractor extractor, PromptBuilder prompts) {
        this.logger = logger;
        this.extractor = extractor;
        this.prompts = prompts;
    }

    public boolean needsPomFix(String testOutput) {
        return testOutput.contains("package") && testOutput.contains("does not exist")
                || testOutput.contains("cannot find symbol")
                || testOutput.contains("Failed to execute goal") && testOutput.contains("dependencies");
    }

    public boolean fixPom(Path projectRoot, String testOutput, OllamaClient ollama) {
        String prompt = prompts.buildPomPrompt(testOutput);
        logger.accept("POM fix prompt:");
        logger.accept(prompt);

        String aiResponse = ollama.call("deepseek-coder-v2:16b", prompt);
        logger.accept("POM fix AI response:");
        logger.accept(aiResponse);

        String depsXml = extractor.extractDependencies(aiResponse);
        if (depsXml == null) {
            logger.accept("No <dependency> blocks found in AI response.");
            return false;
        }

        new PomWriter(logger).addDependencies(projectRoot, depsXml);
        return true;
    }
}
