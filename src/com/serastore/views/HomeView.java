package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class HomeView extends JPanel {

    private String currentUser;

    public HomeView(String username) {
        this.currentUser = username;
        
        setLayout(new BorderLayout(25, 25));
        setOpaque(false);
        initComponent();
    }

    private void initComponent() {
        JPanel cardContainer = new JPanel(new GridLayout(1, 4, 25, 0));
        cardContainer.setOpaque(false);

        cardContainer.add(createStatCard("Total Pendapatan", "Rp " + getTodayRevenue(), new Color(128, 0, 0)));
        cardContainer.add(createStatCard("Transaksi Hari Ini", getTodayTrxCount() + " Trx", new Color(44, 62, 80)));
        cardContainer.add(createStatCard("Stok Menipis!", getLowStockCount() + " Produk", new Color(230, 126, 34)));
        cardContainer.add(createStatCard("Total Produk", getTotalProduct() + " Item", new Color(39, 174, 96)));

        JPanel middlePanel = new JPanel(new BorderLayout(25, 0));
        middlePanel.setOpaque(false);

        JPanel chartBox = new JPanel(new BorderLayout());
        chartBox.setBackground(Color.WHITE);
        chartBox.putClientProperty(FlatClientProperties.STYLE, "arc: 25;");
        chartBox.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblChartTitle = new JLabel("Grafik Pendapatan 7 Hari Terakhir");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        chartBox.add(lblChartTitle, BorderLayout.NORTH);
        chartBox.add(createWeeklyChart(), BorderLayout.CENTER);

        JPanel stockAlertBox = new JPanel(new BorderLayout());
        stockAlertBox.setPreferredSize(new Dimension(400, 0));
        stockAlertBox.setBackground(Color.WHITE);
        stockAlertBox.putClientProperty(FlatClientProperties.STYLE, "arc: 25;");
        stockAlertBox.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblAlertTitle = new JLabel("STOK PERLU PERHATIAN");
        lblAlertTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAlertTitle.setForeground(new Color(50, 50, 50));
        lblAlertTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        stockAlertBox.add(lblAlertTitle, BorderLayout.NORTH);

        JPanel alertContainer = new JPanel();
        alertContainer.setLayout(new BoxLayout(alertContainer, BoxLayout.Y_AXIS));
        alertContainer.setOpaque(false);

        loadStockAlerts(alertContainer);

        JScrollPane scrollAlert = new JScrollPane(alertContainer);
        scrollAlert.setBorder(null);
        scrollAlert.setOpaque(false);
        scrollAlert.getViewport().setOpaque(false);
        scrollAlert.getVerticalScrollBar().setUnitIncrement(16);
        
        stockAlertBox.add(scrollAlert, BorderLayout.CENTER);

        middlePanel.add(chartBox, BorderLayout.CENTER);
        middlePanel.add(stockAlertBox, BorderLayout.EAST);

        add(cardContainer, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(0, 120));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 25;");
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(color);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private ChartPanel createWeeklyChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT DATE(tgl_transaksi) as tgl, SUM(total_akhir) as total " +
                         "FROM transactions WHERE tgl_transaksi >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                         "GROUP BY DATE(tgl_transaksi) ORDER BY tgl ASC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()) {
                dataset.addValue(rs.getDouble("total"), "Pendapatan", rs.getString("tgl"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setOutlineVisible(false);

        org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(128, 0, 0));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.1);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);
        return chartPanel;
    }

    private void loadStockAlerts(JPanel container) {
        container.removeAll(); 
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT id_produk, nama_produk, stok, stok_min FROM products WHERE stok <= stok_min ORDER BY stok ASC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            boolean adaData = false;
            while(rs.next()) {
                adaData = true;
                container.add(createAlertItem(rs.getInt("id_produk"), rs.getString("nama_produk"), rs.getInt("stok")));
                container.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            
            if(!adaData) {
                JLabel lblEmpty = new JLabel("Semua stok aman!", SwingConstants.CENTER);
                lblEmpty.setForeground(Color.GRAY);
                container.add(lblEmpty);
            }
        } catch (Exception e) { e.printStackTrace(); }
        container.revalidate();
        container.repaint();
    }

    private JPanel createAlertItem(int id, String nama, int stok) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setBackground(new Color(255, 248, 240));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        item.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        item.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");

        JLabel lblIcon = new JLabel("!", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(230, 126, 34)); 
        lblIcon.setPreferredSize(new Dimension(35, 35));
        lblIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 999;"); 

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel lblName = new JLabel(nama);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblStatus = new JLabel("Sisa Stok: " + stok);
        lblStatus.setForeground(stok == 0 ? Color.RED : new Color(200, 100, 0));
        info.add(lblName);
        info.add(lblStatus);

        JButton btnRestock = new JButton("Restock");
        btnRestock.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestock.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #FFF; focusWidth: 0;");

        btnRestock.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof JFrame) {
                ProductDialog dialog = new ProductDialog((JFrame) parentWindow, true, null, id, currentUser);
                dialog.setVisible(true);
                
                removeAll();
                initComponent();
                revalidate();
                repaint();
            }
        });

        item.add(lblIcon, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);
        item.add(btnRestock, BorderLayout.EAST);
        return item;
    }

    private String getTodayRevenue() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(total_akhir) FROM transactions WHERE DATE(tgl_transaksi) = CURDATE()");
            if(rs.next()) return String.format("%,.0f", rs.getDouble(1));
        } catch (Exception e) {}
        return "0";
    }

    private int getTodayTrxCount() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM transactions WHERE DATE(tgl_transaksi) = CURDATE()");
            if(rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }

    private int getLowStockCount() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM products WHERE stok <= stok_min");
            if(rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }

    private int getTotalProduct() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM products");
            if(rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }
}