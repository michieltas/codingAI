package nl.mihaly.main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomWriter {

    private final Consumer<String> logger;

    public PomWriter(Consumer<String> logger) {
        this.logger = logger;
    }

    public void addDependencies(Path projectRoot, String depsXml) {
        try {
            Path pom = projectRoot.resolve("pom.xml");
            String pomText = Files.readString(pom, StandardCharsets.UTF_8);

            // 1. Clean AI output
            String cleaned = sanitize(depsXml);

            // 2. Extract dependency blocks
            Pattern depPattern = Pattern.compile("<dependency>[\\s\\S]*?</dependency>");
            Matcher matcher = depPattern.matcher(cleaned);

            List<Dependency> newDeps = new ArrayList<>();

            while (matcher.find()) {
                Dependency dep = Dependency.parse(matcher.group());

                if (dep == null) continue;

                // Only allow JUnit 5
                if (!dep.groupId.equals("org.junit.jupiter")) {
                    logger.accept("Skipping non-JUnit dependency: " + dep);
                    continue;
                }

                // Skip BOMs
                if ("pom".equals(dep.type)) {
                    logger.accept("Skipping BOM dependency: " + dep);
                    continue;
                }

                newDeps.add(dep);
            }

            if (newDeps.isEmpty()) {
                logger.accept("No valid dependencies to add.");
                return;
            }

            // 3. Parse existing pom dependencies
            Map<String, Dependency> existing = extractExistingDependencies(pomText);

            // 4. Merge
            for (Dependency dep : newDeps) {
                String key = dep.groupId + ":" + dep.artifactId;

                if (existing.containsKey(key)) {
                    logger.accept("Updating existing dependency: " + key);
                    existing.put(key, dep); // replace old version
                } else {
                    logger.accept("Adding new dependency: " + key);
                    existing.put(key, dep);
                }
            }

            // 5. Rebuild <dependencies> section
            String rebuilt = rebuildPom(pomText, existing.values());
            Files.writeString(pom, rebuilt, StandardCharsets.UTF_8);

            logger.accept("pom.xml updated cleanly.");

        } catch (Exception e) {
            logger.accept("Failed to update pom.xml: " + e.getMessage());
        }
    }

    private String sanitize(String xml) {
        return xml
                .replaceAll("(?s)<dependencies>.*?</dependencies>", "")
                .replaceAll("<!--.*?-->", "")
                .trim();
    }

    private Map<String, Dependency> extractExistingDependencies(String pom) {
        Map<String, Dependency> map = new LinkedHashMap<>();

        Pattern depPattern = Pattern.compile("<dependency>[\\s\\S]*?</dependency>");
        Matcher matcher = depPattern.matcher(pom);

        while (matcher.find()) {
            Dependency dep = Dependency.parse(matcher.group());
            if (dep != null) {
                map.put(dep.groupId + ":" + dep.artifactId, dep);
            }
        }

        return map;
    }

    private String rebuildPom(String pom, Collection<Dependency> deps) {
        StringBuilder block = new StringBuilder();
        block.append("  <dependencies>\n");

        for (Dependency d : deps) {
            block.append(d.toXml()).append("\n");
        }

        block.append("  </dependencies>");

        // Replace entire dependencies section
        return pom.replaceAll("(?s)<dependencies>.*?</dependencies>", block.toString());
    }

    // ---------------- Dependency helper ----------------

    private static class Dependency {
        String groupId;
        String artifactId;
        String version;
        String scope;
        String type;

        static Dependency parse(String xml) {
            String g = extract(xml, "groupId");
            String a = extract(xml, "artifactId");
            String v = extract(xml, "version");
            String s = extract(xml, "scope");
            String t = extract(xml, "type");

            if (g == null || a == null) return null;

            Dependency d = new Dependency();
            d.groupId = g;
            d.artifactId = a;
            d.version = v;
            d.scope = s;
            d.type = t;
            return d;
        }

        static String extract(String xml, String tag) {
            Matcher m = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">").matcher(xml);
            return m.find() ? m.group(1).trim() : null;
        }

        String toXml() {
            StringBuilder sb = new StringBuilder();
            sb.append("    <dependency>\n");
            sb.append("      <groupId>").append(groupId).append("</groupId>\n");
            sb.append("      <artifactId>").append(artifactId).append("</artifactId>\n");
            if (version != null) sb.append("      <version>").append(version).append("</version>\n");
            if (scope != null) sb.append("      <scope>").append(scope).append("</scope>\n");
            sb.append("    </dependency>");
            return sb.toString();
        }

        public String toString() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }
}
