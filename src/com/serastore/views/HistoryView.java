package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HistoryView extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnRefresh, btnDetail;

    public HistoryView() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        initComponent();
        loadData("");
    }

    private void initComponent() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(350, 40));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari No Faktur...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #FFF; showClearButton: true;");
        
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                loadData(txtSearch.getText());
            }
        });

        btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 40));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        btnRefresh.addActionListener(e -> loadData(""));

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBar.setOpaque(false);
        rightBar.add(btnRefresh);

        topBar.add(txtSearch, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "No Faktur", "Tanggal", "Total Akhir", "Bayar", "Kembali"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.setOpaque(false);

        btnDetail = new JButton("Lihat Detail Transaksi");
        btnDetail.setPreferredSize(new Dimension(200, 40));
        btnDetail.setBackground(new Color(128, 0, 0));
        btnDetail.setForeground(Color.WHITE);
        btnDetail.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; focusWidth: 0;");
        
        btnDetail.addActionListener(e -> showDetailDialog());

        bottomBar.add(btnDetail);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadData(String cari) {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT * FROM transactions WHERE no_faktur LIKE ? ORDER BY id_transaksi DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + cari + "%");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_transaksi"),
                    rs.getString("no_faktur"),
                    rs.getString("tgl_transaksi"),
                    "Rp " + String.format("%,.0f", rs.getDouble("total_akhir")),
                    "Rp " + String.format("%,.0f", rs.getDouble("bayar")),
                    "Rp " + String.format("%,.0f", rs.getDouble("kembali"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

private void showDetailDialog() {
    int row = table.getSelectedRow();
    if (row == -1) return;

    String noFaktur = tableModel.getValueAt(row, 1).toString();
    
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detail: " + noFaktur, true);
    dialog.setSize(700, 450);
    dialog.setLocationRelativeTo(this);
    
    JPanel pnl = new JPanel(new BorderLayout(10, 10));
    pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    DefaultTableModel dtm = new DefaultTableModel(new String[]{"Nama Produk", "Harga Satuan", "Qty", "Subtotal"}, 0);
    JTable t = new JTable(dtm);
    t.setRowHeight(35);

    try {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT p.nama_produk, d.qty, d.subtotal, (d.subtotal/d.qty) as harga " +
                     "FROM transaction_details d " +
                     "JOIN products p ON d.id_produk = p.id_produk " +
                     "WHERE d.no_faktur = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, noFaktur);
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()) {
            dtm.addRow(new Object[]{
                rs.getString("nama_produk"),
                "Rp " + String.format("%,.0f", rs.getDouble("harga")),
                rs.getInt("qty"),
                "Rp " + String.format("%,.0f", rs.getDouble("subtotal"))
            });
        }
    } catch (Exception e) { e.printStackTrace(); }

    pnl.add(new JScrollPane(t), BorderLayout.CENTER);
    
    JButton btn = new JButton("Tutup Detail");
    btn.putClientProperty("FlatLaf.style", "arc: 10; background: #800000; foreground: #fff");
    btn.addActionListener(e -> dialog.dispose());
    pnl.add(btn, BorderLayout.SOUTH);

    dialog.add(pnl);
    dialog.setVisible(true);
}
}