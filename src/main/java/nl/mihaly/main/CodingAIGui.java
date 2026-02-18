package nl.mihaly.main;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * Provides a Swing-based graphical interface for the AI TDD agent.
 *
 * Allows the user to select a project root, enter class details,
 * start the TDD loop, and view real-time log output.
 */
public class CodingAIGui extends JFrame {

    private boolean projectRootChosen = false;

    private final JTextField packageField = new JTextField(30);
    private final JTextField classNameField = new JTextField(30);
    private final JTextArea specArea = new JTextArea(5, 40);
    private final JTextArea logArea = new JTextArea();

    private Path projectRoot = Path.of("C:\\temp\\IdeaProjects");

    private final CacheManager cache;
    private final GuiOrganizer organizer = new GuiOrganizer();

    public CodingAIGui() {
        super("CodingAI TDD Agent");

        this.cache = new CacheManager(this::log);

        setLayout(new BorderLayout());

        buildUiUsingOrganizer();

        setSize(800, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        prefillFields();
    }

    private void buildUiUsingOrganizer() {
        // Build UI using organizer
        JPanel buttonBar = createButtonBar();
        JPanel classPanel = organizer.createClassPanel(classNameField);
        JScrollPane specScroll = organizer.createSpecPanel(specArea);
        JScrollPane logScroll = organizer.createLogPanel(logArea);

        JPanel center = organizer.createCenterPanel(classPanel, specScroll, logScroll);
        JPanel packagePanel = organizer.createPackagePanel(packageField);
        center.add(packagePanel, 0); // add package panel at top

        add(buttonBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void prefillFields() {
        CacheManager.CachedValues cached = cache.load();

        classNameField.setText(cached.className);
        specArea.setText(cached.specification);
        packageField.setText(cached.packageName);
    }

    // ------------------------------------------------------------
    // BUTTON BAR
    // ------------------------------------------------------------

    private JPanel createButtonBar() {
        JButton chooseButton = new JButton("Choose project root");
        JButton startButton = new JButton("Start TDD-loop");
        JButton clearButton = new JButton("Clear");

        chooseButton.addActionListener(e -> chooseProjectRoot());
        startButton.addActionListener(e -> startAgent());
        clearButton.addActionListener(e -> clearFields());

        JPanel bar = new JPanel();
        bar.add(chooseButton);
        bar.add(startButton);
        bar.add(clearButton);

        return bar;
    }

    // ------------------------------------------------------------
    // ACTIONS
    // ------------------------------------------------------------

    private void chooseProjectRoot() {
        JFileChooser chooser = new JFileChooser(projectRoot.toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            projectRoot = chooser.getSelectedFile().toPath();
            projectRootChosen = true;
            log("Selected project root: " + projectRoot);
        }
    }

    private void startAgent() {
        if (!projectRootChosen) {
            log("Please choose a project root first.");
            return;
        }

        String specification = specArea.getText().trim();
        String className = classNameField.getText().trim();
        String packageName = packageField.getText().trim();

        if (specification.isEmpty()) {
            log("Specification cannot be empty.");
            return;
        }

        if (className.isEmpty()) {
            log("Class name cannot be empty.");
            return;
        }

        cache.save(className, specification, packageName);

        log("Starting TDD-loop...");

        new Thread(() -> {
            CodingAIAgent agent = new CodingAIAgent(this::log, specification, className, packageName);
            agent.runFullProcess(projectRoot);
        }).start();
    }

    private void clearFields() {
        classNameField.setText("");
        specArea.setText("");
        packageField.setText("");
        cache.clear();
        log("Fields cleared and cache removed.");
    }

    // ------------------------------------------------------------
    // LOGGING
    // ------------------------------------------------------------

    public void log(String msg) {
        System.out.println(msg);
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    // ------------------------------------------------------------
    // MAIN
    // ------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CodingAIGui().setVisible(true));
    }
}
