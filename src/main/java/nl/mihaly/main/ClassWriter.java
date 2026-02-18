package nl.mihaly.main;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

public class ClassWriter {

    private final Consumer<String> logger;

    public ClassWriter(Consumer<String> logger) {
        this.logger = logger;
    }

    public Path write(String className, String packageName, String javaSource, Path projectRoot) {
        try {
            // 1. Extract package from AI source if present
            String extractedPackage = extractPackage(javaSource);

            // 2. Decide final package
            String finalPackage = (extractedPackage != null && !extractedPackage.isBlank())
                    ? extractedPackage
                    : packageName;

            if (finalPackage == null) {
                finalPackage = "";
            }

            // 3. Build path
            String fullName = finalPackage.isBlank()
                    ? className
                    : finalPackage + "." + className;

            String path = fullName.replace('.', '/') + ".java";
            Path filePath = projectRoot.resolve("src/main/java").resolve(path);

            // 4. Ensure package declaration exists and matches
            if (extractedPackage == null && !finalPackage.isBlank()) {
                javaSource = "package " + finalPackage + ";\n\n" + javaSource;
            }

            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, javaSource);

            logger.accept("Wrote class to: " + filePath);
            return filePath;

        } catch (IOException e) {
            logger.accept("Error writing class file: " + e.getMessage());
            return null;
        }
    }

    private String extractPackage(String javaSource) {
        for (String line : javaSource.split("\n")) {
            line = line.trim();
            if (line.startsWith("package ") && line.endsWith(";")) {
                return line.substring(8, line.length() - 1).trim();
            }
        }
        return null;
    }
}
