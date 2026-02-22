package nl.mihaly.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PomFixer {

    private final Consumer<String> logger;
    private final JavaCodeExtractor extractor;
    private final OllamaClient ollama;
    private final String className;
    private final String packageName;

    public PomFixer(Consumer<String> logger,
                    JavaCodeExtractor extractor,
                    String packageName, String className) {
        this.logger = logger;
        this.extractor = extractor;
        this.ollama = new OllamaClient(logger);
        this.packageName = packageName;
        this.className = className;
    }

    /**
     * NEW: Only fix POM if:
     * 1. The class already exists (otherwise it's a compile error, not a dependency error)
     * 2. The test output contains REAL dependency resolution failures
     */
    public boolean needsPomFix(Path projectRoot, String output) {
        // If the class does not exist yet → NEVER fix the pom
        Path classPath = projectRoot
                .resolve("src/main/java")
                .resolve(packageName.replace('.', '/'))
                .resolve(className + ".java");

        if (!Files.exists(classPath)) {
            return false;
        }

        String lower = output.toLowerCase();

        // True dependency resolution failures
        if (lower.contains("could not resolve dependencies")) return true;
        if (lower.contains("missing artifact")) return true;
        if (lower.contains("was not found in") && lower.contains("repository")) return true;
        if (lower.contains("failed to read artifact descriptor")) return true;
        if (lower.contains("dependencyresolutionexception")) return true;

        return false;
    }

    public boolean fixPom(Path projectRoot, String testOutput) {
        String prompt = Texts.POM_PROMPT.formatted(testOutput);
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

        PomWriter pomWriter = new PomWriter(logger);
        pomWriter.addDependencies(projectRoot, depsXml);

        return true;
    }
}
