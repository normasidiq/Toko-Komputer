import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class loginForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public loginForm() {
        setTitle("Login");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        add(userLabel);
        add(usernameField);
        add(passLabel);
        add(passwordField);
        add(new JLabel()); 
        add(loginButton);

        loginButton.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("id");

                JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + role);
                this.dispose(); 

                if (role.equalsIgnoreCase("admin")) {
                    new adminDashboard();
                } else {
                    new userDashboard(userId);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Username atau password salah.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal login: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new loginForm();
    }
}
