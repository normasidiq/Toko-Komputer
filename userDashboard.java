import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class userDashboard extends JFrame {
    private int userId;
    private JTable produkTable;
    private DefaultTableModel produkModel, keranjangModel;
    private JTextField jumlahField, cariField;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<ItemKeranjang> keranjang = new ArrayList<>();

    public userDashboard(int userId) {
        this.userId = userId;
        setTitle("User - Transaksi Penjualan");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        produkModel = new DefaultTableModel(new String[]{"ID", "Nama", "Kategori", "Harga", "Stok"}, 0);
        produkTable = new JTable(produkModel);
        rowSorter = new TableRowSorter<>(produkModel);
        produkTable.setRowSorter(rowSorter);
        JScrollPane produkScroll = new JScrollPane(produkTable);

        JPanel cariPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cariPanel.add(new JLabel("Cari Produk:"));
        cariField = new JTextField(20);
        cariPanel.add(cariField);

        cariField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }

            private void search() {
                String keyword = cariField.getText();
                if (keyword.trim().isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1, 2)); 
                }
            }
        });

        jumlahField = new JTextField(5);
        JButton tambahBtn = new JButton("Tambah ke Keranjang");

        JPanel jumlahPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jumlahPanel.add(new JLabel("Jumlah:"));
        jumlahPanel.add(jumlahField);
        jumlahPanel.add(tambahBtn);

        JPanel produkPanel = new JPanel(new BorderLayout(10, 10));
        produkPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Daftar Produk", TitledBorder.LEFT, TitledBorder.TOP));
        produkPanel.add(cariPanel, BorderLayout.NORTH);
        produkPanel.add(produkScroll, BorderLayout.CENTER);
        produkPanel.add(jumlahPanel, BorderLayout.SOUTH);

        keranjangModel = new DefaultTableModel(new String[]{"Produk", "Jumlah", "Subtotal"}, 0);
        JTable keranjangTable = new JTable(keranjangModel);
        JScrollPane keranjangScroll = new JScrollPane(keranjangTable);

        JButton hapusBtn = new JButton("Hapus dari Keranjang");
        JButton bayarBtn = new JButton("Bayar");

        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tombolPanel.add(hapusBtn);
        tombolPanel.add(bayarBtn);

        JPanel keranjangPanel = new JPanel(new BorderLayout(10, 10));
        keranjangPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Keranjang", TitledBorder.LEFT, TitledBorder.TOP));
        keranjangPanel.add(keranjangScroll, BorderLayout.CENTER);
        keranjangPanel.add(tombolPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, produkPanel, keranjangPanel);
        splitPane.setResizeWeight(0.5);

        add(splitPane);

        tambahBtn.addActionListener(e -> tambahKeKeranjang());
        hapusBtn.addActionListener(e -> hapusDariKeranjang(keranjangTable));
        bayarBtn.addActionListener(e -> prosesPembayaran());

        loadProduk();
        setVisible(true);

    }

    private void loadProduk() {
        produkModel.setRowCount(0);
        try (Connection conn = DBkoneksi.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM produk");
            while (rs.next()) {
                produkModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("kategori"),
                        rs.getDouble("harga"),
                        rs.getInt("stok")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat produk: " + ex.getMessage());
        }
    }

    private void tambahKeKeranjang() {
    int selectedRow = produkTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu.");
        return;
    }

    selectedRow = produkTable.convertRowIndexToModel(selectedRow);

    int id = (int) produkModel.getValueAt(selectedRow, 0);
    String nama = produkModel.getValueAt(selectedRow, 1).toString();
    double harga = (double) produkModel.getValueAt(selectedRow, 3); 
    int stok = (int) produkModel.getValueAt(selectedRow, 4);        

    int jumlah;
    try {
        jumlah = Integer.parseInt(jumlahField.getText());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Masukkan jumlah yang valid.");
        return;
    }

    if (jumlah <= 0 || jumlah > stok) {
        JOptionPane.showMessageDialog(this, "Jumlah tidak valid atau melebihi stok.");
        return;
    }

    keranjang.add(new ItemKeranjang(id, nama, jumlah, harga));
    keranjangModel.addRow(new Object[]{nama, jumlah, harga * jumlah});
    jumlahField.setText("");
}

    private void hapusDariKeranjang(JTable keranjangTable) {
        int selectedRow = keranjangTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih item yang ingin dihapus dari keranjang.");
            return;
        }

        keranjang.remove(selectedRow);
        keranjangModel.removeRow(selectedRow);
    }

    private void prosesPembayaran() {
        if (keranjang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong.");
            return;
        }

        try (Connection conn = DBkoneksi.getConnection()) {
            conn.setAutoCommit(false);

            double total = keranjang.stream().mapToDouble(i -> i.jumlah * i.harga).sum();

            PreparedStatement transStmt = conn.prepareStatement(
                    "INSERT INTO transaksi (user_id, total) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            transStmt.setInt(1, userId);
            transStmt.setDouble(2, total);
            transStmt.executeUpdate();

            ResultSet rs = transStmt.getGeneratedKeys();
            rs.next();
            int transaksiId = rs.getInt(1);

            for (ItemKeranjang item : keranjang) {
                PreparedStatement detailStmt = conn.prepareStatement(
                        "INSERT INTO detail_transaksi (transaksi_id, produk_id, jumlah, subtotal) VALUES (?, ?, ?, ?)");
                detailStmt.setInt(1, transaksiId);
                detailStmt.setInt(2, item.idProduk);
                detailStmt.setInt(3, item.jumlah);
                detailStmt.setDouble(4, item.harga * item.jumlah);
                detailStmt.executeUpdate();

                PreparedStatement stokStmt = conn.prepareStatement(
                        "UPDATE produk SET stok = stok - ? WHERE id = ?");
                stokStmt.setInt(1, item.jumlah);
                stokStmt.setInt(2, item.idProduk);
                stokStmt.executeUpdate();
            }

            conn.commit();

            ArrayList<String> daftarBarang = new ArrayList<>();
            for (ItemKeranjang item : keranjang) {
                daftarBarang.add(item.nama + " x" + item.jumlah + " = Rp " + (item.harga * item.jumlah));
            }
            String tanggal = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
            strukTransaksi.cetak("User#" + userId, daftarBarang, total, tanggal);

            JOptionPane.showMessageDialog(this, "Transaksi berhasil!");

            keranjang.clear();
            keranjangModel.setRowCount(0);
            loadProduk();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + ex.getMessage());
        }
    }

    private static class ItemKeranjang {
        int idProduk;
        String nama;
        int jumlah;
        double harga;

        ItemKeranjang(int idProduk, String nama, int jumlah, double harga) {
            this.idProduk = idProduk;
            this.nama = nama;
            this.jumlah = jumlah;
            this.harga = harga;
        }
    }
}
