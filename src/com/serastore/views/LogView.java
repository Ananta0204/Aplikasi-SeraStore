package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class LogView extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    public LogView() {
        setLayout(new BorderLayout(0, 25));
        setOpaque(false);
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("RIWAYAT AKTIVITAS SISTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(50, 50, 50));
        
        JButton btnRefresh = new JButton("Segarkan Data");
        btnRefresh.setPreferredSize(new Dimension(150, 40));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        btnRefresh.addActionListener(e -> loadData());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        tableModel = new DefaultTableModel(new String[]{"Waktu Kejadian", "Pelaksana (User)", "Aksi / Deskripsi Kegiatan"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(600);

        JScrollPane scroll = new JScrollPane(table);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");
        scroll.setBorder(BorderFactory.createEmptyBorder());

        add(headerPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT l.waktu, u.nama_lengkap, l.aksi FROM activity_logs l " +
                         "JOIN users u ON l.id_user = u.id_user ORDER BY l.id_log DESC LIMIT 100";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3)
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}