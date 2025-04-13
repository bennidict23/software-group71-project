package org.example;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private UserManager userManager;

    public RegisterFrame() {
        super("Register");
        userManager = new UserManager();

        setSize(300, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField();

        JButton registerButton = new JButton("Register");

        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(confirmPassLabel);
        panel.add(confirmPasswordField);
        panel.add(new JLabel());
        panel.add(registerButton);

        add(panel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> performRegistration());
    }

    private void performRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        boolean success = userManager.registerUser(username, password);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

