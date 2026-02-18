package nl.mihaly.main;

/**
 * Holds the prompt templates and other texts.
 *
 * Provides reusable text blocks for the main model and fallback model prompts.
 */
public interface Texts {

    String MVN = "C:\\Program Files\\Maven\\apache-maven-3.9.12\\bin\\mvn.cmd";

    String PROMPT = """
    You are an AI Java TDD assistant.

    The user wants you to work ONLY on the following class:
    %s

    This class MUST be placed in the following package (if not empty):
    %s

    If the class does not exist yet, create it.
    If it already exists, rewrite the entire file.

    Specification / intended behavior:
    %s

    Here is the full JUnit test class that must pass:
    %s

    Below is the JUnit test output showing the failures.
    Your task:
    - Produce the FULL Java source code for the class.
    - The class MUST start with the correct package declaration if provided.
    - Modify ONLY the specified class.
    - Ensure the logic satisfies the specification and the tests.
    - Do NOT create or modify any other files.
    - Do NOT include explanations, comments, or prose.
    - Output ONLY the Java source code.
    - Wrap the code in a ```java ... ``` block.

    Test output:
    %s
    """;

    String FALLBACKPROMPT = """
    You are a high‑reasoning Java expert (deepseek-r1-70b).

    The smaller model failed to fix the class after 30 attempts.
    Now you must produce a fully correct solution.

    The user wants you to work ONLY on the following class:
    %s

    This class MUST be placed in the following package (if not empty):
    %s

    If the class does not exist yet, create it.
    If it already exists, rewrite the entire file.

    Specification / intended behavior:
    %s

    Here is the full JUnit test class that must pass:
    %s

    Below is the JUnit test output showing the failures.
    Your task:
    - Carefully analyze the specification, the test class, and the test failures.
    - Produce the FULL Java source code for the class.
    - The class MUST start with the correct package declaration if provided.
    - Modify ONLY the specified class.
    - Ensure the logic is complete and all tests pass.
    - Do NOT create or modify any other files.
    - Do NOT include explanations, comments, or prose.
    - Output ONLY the Java source code.
    - Wrap the code in a ```java ... ``` block.

    Test output:
    %s
    """;

    String POM_PROMPT = """
    You are an AI Maven dependency expert.

    The Java code failed to compile due to missing dependencies.

    Your task:
    - Analyze the test output.
    - Produce ONLY the <dependency> entries that must be added to pom.xml.
    - Do NOT output the full pom.xml.
    - Do NOT include explanations or comments.
    - Output ONLY a ```xml ... ``` block containing one or more <dependency> elements.

    Test output:
    %s
    """;
}
