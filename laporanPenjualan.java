import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class laporanPenjualan extends JFrame {
    private JTable transaksiTable, detailTable;
    private DefaultTableModel transaksiModel, detailModel;

    public laporanPenjualan() {
        setTitle("Laporan Penjualan");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        transaksiModel = new DefaultTableModel(new String[]{"ID", "User", "Tanggal", "Total"}, 0);
        transaksiTable = new JTable(transaksiModel);
        JScrollPane transaksiScroll = new JScrollPane(transaksiTable);

        detailModel = new DefaultTableModel(new String[]{"Produk", "Jumlah", "Subtotal"}, 0);
        detailTable = new JTable(detailModel);
        JScrollPane detailScroll = new JScrollPane(detailTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transaksiScroll, detailScroll);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        loadTransaksi();

        transaksiTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanDetail();
            }
        });

        setVisible(true);
    }

    private void loadTransaksi() {
        transaksiModel.setRowCount(0);
        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT t.id, u.username, t.tanggal, t.total " +
                         "FROM transaksi t JOIN users u ON t.user_id = u.id ORDER BY t.tanggal DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                transaksiModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getTimestamp("tanggal"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data transaksi: " + e.getMessage());
        }
    }

    private void tampilkanDetail() {
        int row = transaksiTable.getSelectedRow();
        if (row == -1) return; 

        int transaksiId = (int) transaksiModel.getValueAt(row, 0); 
        detailModel.setRowCount(0); 

        try (Connection conn = DBkoneksi.getConnection()) {
            String sql = "SELECT p.nama, d.jumlah, d.subtotal " +
                         "FROM detail_transaksi d " +
                         "JOIN produk p ON d.produk_id = p.id " +
                         "WHERE d.transaksi_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, transaksiId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                detailModel.addRow(new Object[]{
                    rs.getString("nama"),
                    rs.getInt("jumlah"),
                    rs.getDouble("subtotal")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat detail transaksi: " + e.getMessage());
        }
    }
}
