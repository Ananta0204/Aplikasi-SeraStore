package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import com.serastore.utils.Logger;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StockView extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private String currentUsername;

    public StockView(String username) {
        this.currentUsername = username;
        
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
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari nama barang atau barcode...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #FFFFFF; showClearButton: true;");
        
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadData(txtSearch.getText());
            }
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnAdd = new JButton("Tambah Barang");
        btnAdd.setBackground(new Color(128, 0, 0));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(150, 40)); 
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; focusWidth: 0; font: bold;");

        btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 40)); 
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAdd);

        topBar.add(txtSearch, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "Barcode", "Nama Produk", "Kategori", "Harga Jual", "Stok", "Satuan"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45); 
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomBar.setOpaque(false);

        btnEdit = new JButton("Edit Terpilih");
        btnEdit.setPreferredSize(new Dimension(130, 40)); 
        btnEdit.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        btnDelete = new JButton("Hapus Barang");
        btnDelete.setPreferredSize(new Dimension(130, 40)); 
        btnDelete.setForeground(new Color(200, 0, 0));
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        bottomBar.add(btnEdit);
        bottomBar.add(btnDelete);

        
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih baris yang mau diedit dulu cuy!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            
            ProductDialog dialog = new ProductDialog(parentFrame, true, this, id, currentUsername);
            dialog.setVisible(true);
        });

        btnAdd.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            ProductDialog dialog = new ProductDialog(parentFrame, true, this, -1, currentUsername);
            dialog.setVisible(true);
        });

        btnDelete.addActionListener(e -> prosesHapus());
        
        btnRefresh.addActionListener(e -> loadData(""));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData(String cari) {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT p.id_produk, p.barcode, p.nama_produk, p.id_kategori, p.harga_jual, p.stok, p.satuan " +
                         "FROM products p " +
                         "WHERE p.nama_produk LIKE ? OR p.barcode LIKE ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + cari + "%");
            ps.setString(2, "%" + cari + "%");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_produk"),
                    rs.getString("barcode"),
                    rs.getString("nama_produk"),
                    rs.getString("id_kategori"),
                    rs.getDouble("harga_jual"), 
                    rs.getInt("stok"),
                    rs.getString("satuan")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error Load: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void prosesHapus() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang mau dihapus!");
            return;
        }

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String nama = (String) tableModel.getValueAt(row, 2);

        int konfirm = JOptionPane.showConfirmDialog(this, 
                "Yakin mau hapus " + nama + "?\nData yang dihapus tidak bisa kembali.", 
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (konfirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id_produk = ?");
                ps.setInt(1, id);
                int result = ps.executeUpdate();
                
                if (result > 0) {
                    Logger.addLog(currentUsername, "Menghapus barang: " + nama);                  
                    loadData("");
                    JOptionPane.showMessageDialog(this, "Barang berhasil dihapus!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
            }
        }
    }
}