package nl.mihaly.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class TestSourceLoader {

    private final Consumer<String> logger;
    private final String packageName;
    private final String className;

    public TestSourceLoader(Consumer<String> logger, String packageName, String className) {
        this.logger = logger;
        this.packageName = packageName;
        this.className = className;
    }

    public String loadTestSource(Path projectRoot) {
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
}
