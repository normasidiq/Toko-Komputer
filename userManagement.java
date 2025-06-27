import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class userManagement extends JFrame {
    private JTextField txtUsername, txtPassword, txtRole;
    private JTable tableUser;
    private DefaultTableModel model;
    private int selectedId = -1;

    public userManagement() {
        setTitle("Manajemen User");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelInput = new JPanel(new GridLayout(4, 2, 10, 10));
        panelInput.setBorder(BorderFactory.createTitledBorder("Form User"));

        txtUsername = new JTextField();
        txtPassword = new JTextField();
        txtRole = new JTextField();

        panelInput.add(new JLabel("Username:"));
        panelInput.add(txtUsername);
        panelInput.add(new JLabel("Password:"));
        panelInput.add(txtPassword);
        panelInput.add(new JLabel("Role:"));
        panelInput.add(txtRole);

        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBtn.add(btnTambah);
        panelBtn.add(btnUpdate);
        panelBtn.add(btnHapus);

        model = new DefaultTableModel(new String[]{"ID", "Username", "Password", "Role"}, 0);
        tableUser = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tableUser);

        JButton btnBack = new JButton("Kembali ke Dashboard");

        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.add(scrollPane, BorderLayout.CENTER);
        panelBottom.add(btnBack, BorderLayout.SOUTH);

        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.add(panelInput, BorderLayout.NORTH);
        panelAtas.add(panelBtn, BorderLayout.SOUTH);

        add(panelAtas, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.CENTER);

        btnTambah.addActionListener(e -> tambahUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnHapus.addActionListener(e -> hapusUser());

        btnBack.addActionListener(e -> {
            new adminDashboard();
            dispose();
        });

        tableUser.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tableUser.getSelectedRow();
                if (row >= 0) {
                    selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtUsername.setText(model.getValueAt(row, 1).toString());
                    txtPassword.setText(model.getValueAt(row, 2).toString());
                    txtRole.setText(model.getValueAt(row, 3).toString());
                    System.out.println("User dipilih, ID: " + selectedId); // debug
                }
            }
        });

        loadUser();
        setVisible(true);
    }

    private void loadUser() {
        model.setRowCount(0);
        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat user: " + e.getMessage());
        }
    }

    private void tambahUser() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = txtRole.getText();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();

            clearForm();
            loadUser();
            JOptionPane.showMessageDialog(this, "User berhasil ditambahkan.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal tambah user: " + e.getMessage());
        }
    }

    private void updateUser() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user terlebih dahulu!");
            return;
        }

        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = txtRole.getText();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setInt(4, selectedId);
            ps.executeUpdate();

            clearForm();
            loadUser();
            JOptionPane.showMessageDialog(this, "User berhasil diupdate.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal update user: " + e.getMessage());
        }
    }

    private void hapusUser() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user terlebih dahulu!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus user ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedId);
            ps.executeUpdate();

            clearForm();
            loadUser();
            JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus user: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtRole.setText("");
        selectedId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(userManagement::new);
    }
}
