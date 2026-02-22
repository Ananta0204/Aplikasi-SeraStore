package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserView extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public UserView() {
        setLayout(new BorderLayout(0, 25));
        setOpaque(false);
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("MANAJEMEN PENGGUNA (USER)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 40));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        btnAdd = new JButton("Tambah User Baru");
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setBackground(new Color(128, 0, 0));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; font: bold;");

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAdd);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Nama Lengkap", "Role / Jabatan"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JScrollPane scroll = new JScrollPane(table);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomBar.setOpaque(false);

        btnEdit = new JButton("Edit User");
        btnEdit.setPreferredSize(new Dimension(130, 40));
        btnEdit.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        btnDelete = new JButton("Hapus Akun");
        btnDelete.setPreferredSize(new Dimension(130, 40));
        btnDelete.setForeground(new Color(200, 0, 0));
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        bottomBar.add(btnEdit);
        bottomBar.add(btnDelete);

        btnRefresh.addActionListener(e -> loadData());
        
        btnAdd.addActionListener(e -> {
            new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), true, this, -1).setVisible(true);
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), true, this, id).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            }
        });

        btnDelete.addActionListener(e -> hapusUser());

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_user, username, nama_lengkap, role FROM users");
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void hapusUser() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String user = tableModel.getValueAt(row, 1).toString();

        if (user.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Admin utama gak boleh dihapus cuy!");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Hapus akun " + user + "?", "Konfirmasi", 0) == 0) {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id_user=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                loadData();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}