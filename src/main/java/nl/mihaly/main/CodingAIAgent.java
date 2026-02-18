package nl.mihaly.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Coordinates the AI‑driven TDD workflow.
 *
 * This class manages the full development loop:
 * running tests, generating prompts, requesting fixes from the AI,
 * extracting Java code, and writing updated class files.
 */
public class CodingAIAgent {

    private final Consumer<String> logger;
    private final String specification;
    private final String className;
    private final String packageName;

    private final MavenRunner maven;
    private final OllamaClient ollama;
    private final JavaCodeExtractor extractor;
    private final ClassWriter writer;

    public CodingAIAgent(Consumer<String> logger,
                         String specification,
                         String className,
                         String packageName) {

        this.logger = logger;
        this.specification = specification;
        this.className = className;
        this.packageName = packageName;

        this.maven = new MavenRunner(logger);
        this.ollama = new OllamaClient(logger);
        this.extractor = new JavaCodeExtractor(logger);
        this.writer = new ClassWriter(logger);
    }

    /**
     * Runs two full TDD cycles. Each cycle may contain up to 30 iterations.
     */
    public void runFullProcess(Path projectRoot) {
        long start = System.currentTimeMillis();

        for (int cycle = 1; cycle <= 2; cycle++) {
            logger.accept("=== Starting TDD cycle " + cycle + " ===");

            boolean success = runTddLoop(projectRoot);

            if (success) {
                logger.accept("All tests green after cycle " + cycle + "!");

                long end = System.currentTimeMillis();
                long duration = end - start;

                logger.accept("All tests green in " + duration + " ms");

                return;
            }

            logger.accept("Cycle " + cycle + " did not fully succeed.");
        }

        logger.accept("Both cycles completed. Tests still not green.");
    }

    /**
     * Runs up to 30 iterations of the TDD loop using the main model.
     */
    public boolean runTddLoop(Path projectRoot) {
        String lastTestOutput = "";
        String testSource = loadTestSource(projectRoot);

        for (int iteration = 1; iteration <= 30; iteration++) {
            logger.accept("=== Iteration " + iteration + " ===");

            lastTestOutput = maven.runTests(projectRoot);

            // ------------------------------------------------------------
            // NEW LOGIC: Only fix POM if the class already exists AND
            // the error is a real dependency resolution failure.
            // ------------------------------------------------------------
            if (needsPomFix(projectRoot, lastTestOutput)) {
                logger.accept("Dependency resolution errors detected. Attempting to fix pom.xml...");

                if (fixPom(projectRoot, lastTestOutput)) {
                    logger.accept("pom.xml updated. Re-running tests...");
                    continue;
                } else {
                    logger.accept("Failed to fix pom.xml.");
                }
            }

            if (maven.testsGreen(lastTestOutput)) {
                logger.accept("All tests green!");
                return true;
            }

            logger.accept("Test failures detected:");
            logger.accept(lastTestOutput);

            String prompt = Texts.PROMPT.formatted(
                    className,
                    packageName == null ? "" : packageName,
                    specification,
                    testSource,
                    lastTestOutput
            );

            logger.accept("Prompt sent to model:");
            logger.accept(prompt);

            String aiResponse = ollama.call("deepseek-coder-v2:16b", prompt);
            logger.accept("AI response:");
            logger.accept(aiResponse);

            String javaSource = extractor.extract(aiResponse);
            if (javaSource == null) {
                logger.accept("No valid Java code found.");
                continue;
            }

            logger.accept("Extracted Java class:");
            logger.accept(javaSource);

            writer.write(className, packageName, javaSource, projectRoot);
            logger.accept("Class written. Re-running tests...");
        }

        logger.accept("Primary model stuck after 30 iterations.");
        logger.accept("Switching to deepseek-r1-70b for final attempt...");

        return runFallbackModel(projectRoot, lastTestOutput, testSource);
    }

    /**
     * NEW: Only fix POM if:
     * 1. The class already exists (otherwise it's a compile error, not a dependency error)
     * 2. The test output contains REAL dependency resolution failures
     */
    private boolean needsPomFix(Path projectRoot, String output) {
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

    private boolean fixPom(Path projectRoot, String testOutput) {
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

    private String loadTestSource(Path projectRoot) {
        try {
            if (packageName == null || packageName.isBlank()) {
                logger.accept("No package name provided, cannot locate test class reliably.");
                return "";
            }

            Path testPath = projectRoot
                    .resolve("src/test/java")
                    .resolve(packageName.replace('.', '/'))
                    .resolve(className + "Test.java");

            if (Files.exists(testPath)) {
                String src = Files.readString(testPath);
                logger.accept("Loaded test class: " + testPath);
                return src;
            } else {
                logger.accept("Test class not found: " + testPath);
                return "";
            }
        } catch (Exception e) {
            logger.accept("Failed to read test class: " + e.getMessage());
            return "";
        }
    }

    private boolean runFallbackModel(Path projectRoot, String testOutput, String testSource) {
        String prompt = Texts.FALLBACKPROMPT.formatted(
                className,
                packageName == null ? "" : packageName,
                specification,
                testSource,
                testOutput
        );

        String aiResponse = ollama.call("deepseek-r1:70b", prompt);

        logger.accept("Fallback model response:");
        logger.accept(aiResponse);

        String javaSource = extractor.extract(aiResponse);
        if (javaSource != null) {
            writer.write(className, packageName, javaSource, projectRoot);
            logger.accept("Fallback model wrote a full class. Re-running tests...");

            String result = maven.runTests(projectRoot);
            return maven.testsGreen(result);
        }

        logger.accept("Fallback model did not return valid Java code.");
        return false;
    }
}
