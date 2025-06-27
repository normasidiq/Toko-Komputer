import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class adminDashboard extends JFrame {
    private JTextField txtNama, txtKategori, txtHarga, txtStok, txtSearch;
    private JTable tableProduk;
    private DefaultTableModel model;
    private JComboBox<String> filterKategori;
    private int selectedId = -1;

    public adminDashboard() {
        setTitle("Admin - Manajemen Produk");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelInput = new JPanel();
        panelInput.setBorder(BorderFactory.createTitledBorder("Form Produk"));

        JLabel lblNama = new JLabel("Nama Produk:");
        JLabel lblKategori = new JLabel("Kategori:");
        JLabel lblHarga = new JLabel("Harga:");
        JLabel lblStok = new JLabel("Stok:");

        txtNama = new JTextField(15);
        txtKategori = new JTextField(15);
        txtHarga = new JTextField(15);
        txtStok = new JTextField(15);

        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");

        GroupLayout layout = new GroupLayout(panelInput);
        panelInput.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblNama)
                    .addComponent(lblHarga)
                    .addComponent(btnUpdate))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(txtNama)
                    .addComponent(txtHarga)
                    .addComponent(btnHapus))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblKategori)
                    .addComponent(lblStok)
                    .addComponent(btnTambah))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(txtKategori)
                    .addComponent(txtStok))
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNama)
                    .addComponent(txtNama)
                    .addComponent(lblKategori)
                    .addComponent(txtKategori))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHarga)
                    .addComponent(txtHarga)
                    .addComponent(lblStok)
                    .addComponent(txtStok))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpdate)
                    .addComponent(btnHapus)
                    .addComponent(btnTambah))
        );

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFilter.setBorder(BorderFactory.createTitledBorder("Filter & Pencarian"));

        txtSearch = new JTextField(20);
        JButton btnCari = new JButton("Cari");

        filterKategori = new JComboBox<>();
        filterKategori.addItem("Semua Kategori");
        loadKategori();

        JButton btnManajemenUser = new JButton("Manajemen User");
        JButton btnLaporanPenjualan = new JButton("Laporan Penjualan");

        panelFilter.add(new JLabel("Cari Nama/Kategori:"));
        panelFilter.add(txtSearch);
        panelFilter.add(btnCari);
        panelFilter.add(new JLabel("Filter Kategori:"));
        panelFilter.add(filterKategori);
        panelFilter.add(btnManajemenUser);
        panelFilter.add(btnLaporanPenjualan);

        model = new DefaultTableModel(new String[]{"ID", "Nama", "Kategori", "Harga", "Stok"}, 0);
        tableProduk = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tableProduk);

        setLayout(new BorderLayout());
        add(panelInput, BorderLayout.NORTH);
        add(panelFilter, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        btnTambah.addActionListener(e -> tambahProduk());
        btnUpdate.addActionListener(e -> updateProduk());
        btnHapus.addActionListener(e -> hapusProduk());
        btnCari.addActionListener(e -> cariProduk());
        filterKategori.addActionListener(e -> filterProduk());

        btnManajemenUser.addActionListener(e -> {
            new userManagement();
            dispose();
        });

        btnLaporanPenjualan.addActionListener(e -> new laporanPenjualan());

        tableProduk.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tableProduk.getSelectedRow();
                if (row >= 0) {
                    selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtNama.setText(model.getValueAt(row, 1).toString());
                    txtKategori.setText(model.getValueAt(row, 2).toString());
                    txtHarga.setText(model.getValueAt(row, 3).toString());
                    txtStok.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        loadProduk();
        setVisible(true);
    }

    private void loadProduk() {
        model.setRowCount(0);
        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT * FROM produk";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kategori"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void loadKategori() {
        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT DISTINCT kategori FROM produk";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                filterKategori.addItem(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat kategori: " + e.getMessage());
        }
    }

    private void tambahProduk() {
        String nama = txtNama.getText();
        String kategori = txtKategori.getText();
        String hargaText = txtHarga.getText();
        String stokText = txtStok.getText();

        if (nama.isEmpty() || kategori.isEmpty() || hargaText.isEmpty() || stokText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try {
            double harga = Double.parseDouble(hargaText);
            int stok = Integer.parseInt(stokText);

            Connection conn = DBkoneksi.getConnection();
            String sql = "INSERT INTO produk (nama, kategori, harga, stok) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, kategori);
            ps.setDouble(3, harga);
            ps.setInt(4, stok);
            ps.executeUpdate();

            clearForm();
            loadProduk();
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Harga dan stok harus berupa angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah produk: " + e.getMessage());
        }
    }

    private void updateProduk() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!");
            return;
        }

        String nama = txtNama.getText();
        String kategori = txtKategori.getText();
        String hargaText = txtHarga.getText();
        String stokText = txtStok.getText();

        if (nama.isEmpty() || kategori.isEmpty() || hargaText.isEmpty() || stokText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try {
            double harga = Double.parseDouble(hargaText);
            int stok = Integer.parseInt(stokText);

            Connection conn = DBkoneksi.getConnection();
            String sql = "UPDATE produk SET nama=?, kategori=?, harga=?, stok=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, kategori);
            ps.setDouble(3, harga);
            ps.setInt(4, stok);
            ps.setInt(5, selectedId);
            ps.executeUpdate();

            clearForm();
            loadProduk();
            JOptionPane.showMessageDialog(this, "Produk berhasil diupdate.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Harga dan stok harus berupa angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal update produk: " + e.getMessage());
        }
    }

    private void hapusProduk() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!");
            return;
        }

        try (Connection conn = DBkoneksi.getConnection()) {
            String cekSql = "SELECT COUNT(*) FROM detail_transaksi WHERE produk_id = ?";
            PreparedStatement cekStmt = conn.prepareStatement(cekSql);
            cekStmt.setInt(1, selectedId);
            ResultSet rs = cekStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Produk tidak bisa dihapus karena sudah digunakan dalam transaksi.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus produk ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String sql = "DELETE FROM produk WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedId);
            ps.executeUpdate();

            clearForm();
            loadProduk();
            JOptionPane.showMessageDialog(this, "Produk berhasil dihapus.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus produk: " + e.getMessage());
        }
    }

    private void cariProduk() {
        String keyword = txtSearch.getText();
        model.setRowCount(0);

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT * FROM produk WHERE nama LIKE ? OR kategori LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kategori"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencari produk: " + e.getMessage());
        }
    }

    private void filterProduk() {
        String kategori = (String) filterKategori.getSelectedItem();
        model.setRowCount(0);

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = kategori.equals("Semua Kategori")
                    ? "SELECT * FROM produk"
                    : "SELECT * FROM produk WHERE kategori=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!kategori.equals("Semua Kategori")) {
                ps.setString(1, kategori);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kategori"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal filter produk: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtNama.setText("");
        txtKategori.setText("");
        txtHarga.setText("");
        txtStok.setText("");
        selectedId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(adminDashboard::new);
    }
}
