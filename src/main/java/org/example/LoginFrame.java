package org.example;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserManager userManager;

    public LoginFrame() {
        super("Login");
        userManager = new UserManager();

        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> openRegisterFrame());
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (userManager.authenticate(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            // 登录成功后打开主页面（例如 FinanceTracker ）
            SwingUtilities.invokeLater(() -> new FinanceTrack().setVisible(true));
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterFrame() {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

