package nl.mihaly.main;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Executes Maven test runs and collects their output.
 *
 * Provides methods to run the test suite and determine whether all tests passed.
 */
public class MavenRunner implements Texts {

    private final Consumer<String> logger;

    public MavenRunner(Consumer<String> logger) {
        this.logger = logger;
    }

    public String runTests(Path root) {
        try {
            String mvnCmd = "C:\\Program Files\\Maven\\apache-maven-3.9.12\\bin\\mvn.cmd";

            ProcessBuilder pb = new ProcessBuilder(
                    mvnCmd,
                    "-Dstyle.color=never",
                    "test"
            );

            pb.directory(root.toFile());
            pb.redirectErrorStream(true);

            Process p = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream())
            );

            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(l -> sb.append(l).append("\n"));

            return sb.toString();

        } catch (IOException e) {
            return "Error running Maven: " + e.getMessage();
        }
    }


    public boolean testsGreen(String output) {
        return output.contains("BUILD SUCCESS");
    }
}
