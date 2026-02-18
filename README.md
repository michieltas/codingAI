README

# CodingAI — AI‑Driven TDD Engine for Java

CodingAI is an experimental but powerful **AI‑driven TDD agent** that automatically generates Java classes based on:

- a **specification** describing the intended functionality  
- a **Maven project containing unit tests**  
- an iterative **Test‑Driven Development (TDD)** workflow  

The agent runs Maven tests, analyzes failures, generates code using an AI model, writes the Java class into the project, and repeats this process until **all tests pass**.

CodingAI is designed as a proof‑of‑concept for AI‑assisted software development, and it works surprisingly robustly and fully autonomously.

---

## 📸 GUI Screenshot

![CodingAI TDD Agent GUI](docs/gui.png)

### How to use the GUI

1. **Select the Maven project root**  
   Choose the main directory of the Maven project where the Java class should be generated.  
   The project must follow the standard Maven directory structure.

2. **Enter the class name and package name**  
   This is the class that CodingAI will generate.

3. **Ensure a matching unit test class exists**  
   The test class must follow standard Maven conventions and must have the same base name.  
   Example:  
   - Class to generate: `PrimeNumbers`  
   - Test class: `PrimeNumbersTest`

4. **Provide a specification**  
   This ensures the generated class not only satisfies the unit tests but also implements the intended functionality.

5. **Start the TDD loop**  
   The bottom panel shows the log output.  
   CodingAI will run one or more iterations until all tests pass.  
   **Note:** If the class already exists, it will be overwritten.

### Example of a generated Java class inside a Maven project

![Generated class example](docs/gegenereerd.png)

---

## 🧱 Requirements

To use CodingAI, you need the following:

### **1. Java 17 or higher**
Required for both CodingAI and the target Maven project.

### **2. Maven installed**
On Windows, `mvn.cmd` must be available in your PATH, for example:

C:\Program Files\Maven\apache-maven-3.9.12\bin\


CodingAI invokes Maven using `ProcessBuilder`.

### **3. Ollama installed**
Download from: https://ollama.com

### **4. AI models**
CodingAI uses:

- `deepseek-coder-v2:16b` (primary model)  
- `deepseek-r1:70b` (fallback model)

Install them via:

```bash
ollama pull deepseek-coder-v2:16b
ollama pull deepseek-r1:70b
