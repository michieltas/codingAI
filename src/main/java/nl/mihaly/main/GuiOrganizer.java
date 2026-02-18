package nl.mihaly.main;

import javax.swing.*;
import java.awt.*;

public class GuiOrganizer {

    public JPanel createPackagePanel(JTextField packageField) {
        packageField.setPreferredSize(new Dimension(400, 28));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Package name (optional)"));
        panel.add(packageField);

        return panel;
    }

    public JPanel createClassPanel(JTextField classNameField) {
        classNameField.setPreferredSize(new Dimension(400, 28));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Class name (to generate/modify)"));
        panel.add(classNameField);

        return panel;
    }

    public JScrollPane createSpecPanel(JTextArea specArea) {
        specArea.setBorder(BorderFactory.createTitledBorder("Specification / intended behavior"));
        specArea.setLineWrap(true);
        specArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(specArea);
        scroll.setPreferredSize(new Dimension(650, 150));
        return scroll;
    }

    public JScrollPane createLogPanel(JTextArea logArea) {
        logArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Log output"));
        scroll.setPreferredSize(new Dimension(650, 250));

        return scroll;
    }

    public JPanel createCenterPanel(JPanel classPanel, JScrollPane specScroll, JScrollPane logScroll) {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(classPanel);
        center.add(specScroll);
        center.add(logScroll);
        return center;
    }
}
