import javax.swing.*;
import java.util.List;

public class strukTransaksi {
    public static void cetak(String kasir, List<String> daftarBarang, double total, String tanggal) {
        StringBuilder struk = new StringBuilder();
        struk.append("====== TOKO KOMPUTER ======\n");
        struk.append("Tanggal: ").append(tanggal).append("\n");
        struk.append("Kasir : ").append(kasir).append("\n");
        struk.append("----------------------------\n");
        for (String item : daftarBarang) {
            struk.append(item).append("\n");
        }
        struk.append("----------------------------\n");
        struk.append("Total : Rp ").append(String.format("%,.2f", total)).append("\n");
        struk.append("===========================\n");

        JTextArea area = new JTextArea(struk.toString());
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        JOptionPane.showMessageDialog(null, new JScrollPane(area), "Struk Transaksi", JOptionPane.INFORMATION_MESSAGE);
    }
}
