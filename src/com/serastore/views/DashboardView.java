package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.github.sarxos.webcam.Webcam;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardView extends JFrame {

    private JPanel sidebar, header, mainContent;
    private JLabel lblCurrentMenu, lblClock;
    private String username, role;
    private boolean isDarkMode = false;

    public DashboardView(String username, String role) {
        this.username = username;
        this.role = role;

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initComponent();
        startClock(); 

        showPanel(new HomeView(username));
    }

    private void initComponent() {
        setLayout(new BorderLayout());

        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(280, 1080));
        sidebar.setBackground(new Color(110, 0, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        logoPanel.setOpaque(false);
        JLabel lblLogo = new JLabel("SERA STORE");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(Color.WHITE);
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));

        String[] menus = {"Dashboard", "Kasir (POS)", "Manajemen Stok", "Riwayat Transaksi", "Laporan", "Log Aktivitas", "Pengaturan"};
        
        for (String m : menus) {
            if (role.equalsIgnoreCase("Kasir")) {
                if (m.equals("Laporan") || m.equals("Log Aktivitas") || m.equals("Pengaturan")) {
                    continue; 
                }
            }
            menuPanel.add(createMenuButton(m));
        }
        sidebar.add(menuPanel, BorderLayout.CENTER);
        
        JButton btnLogout = new JButton("Logout System");
        btnLogout.setPreferredSize(new Dimension(200, 40));
        btnLogout.setBackground(new Color(80, 0, 0));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Keluar dari sistem?", "Logout", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginView().setVisible(true);
            }
        });
        
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(btnLogout);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);

        header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(1640, 80));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(0, 30, 0, 30)
        ));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 25));
        leftHeader.setOpaque(false);
        lblCurrentMenu = new JLabel("Dashboard Overview");
        lblCurrentMenu.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCurrentMenu.setForeground(new Color(50, 50, 50));
        leftHeader.add(lblCurrentMenu);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightHeader.setOpaque(false);

        JButton btnTheme = new JButton("🌙");
        btnTheme.setPreferredSize(new Dimension(45, 45));
        btnTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 999; background: #f0f0f0; borderWidth: 0; focusWidth: 0;");
        btnTheme.addActionListener(e -> toggleTheme(btnTheme));

        lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lblClock.setForeground(new Color(100, 100, 100));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        JLabel name = new JLabel(username);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel badge = new JLabel(" " + role.toUpperCase() + " ");
        badge.setOpaque(true);
        badge.setBackground(new Color(240, 240, 240));
        badge.setForeground(new Color(128, 0, 0));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        userInfo.add(name);
        userInfo.add(badge);

        JLabel avatar = new JLabel(String.valueOf(username.charAt(0)).toUpperCase(), SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(45, 45));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(128, 0, 0));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avatar.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");

        rightHeader.add(btnTheme);
        rightHeader.add(lblClock);
        rightHeader.add(new JSeparator(JSeparator.VERTICAL));
        rightHeader.add(userInfo);
        rightHeader.add(avatar);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(248, 249, 250));
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        add(sidebar, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(240, 50));
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        btn.setForeground(new Color(220, 220, 220));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 25, 0, 0));
        btn.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #900000; arc: 12;");

        btn.addActionListener(e -> {
            if (text.equals("Dashboard")) {
                lblCurrentMenu.setText("Dashboard Overview");
                showPanel(new HomeView(username));
            } else if (text.equals("Kasir (POS)")) {
                lblCurrentMenu.setText("Point of Sale (POS)");
                showPanel(new TransactionView());
            } else if (text.equals("Manajemen Stok")) {
                lblCurrentMenu.setText("Inventory Management");
                showPanel(new StockView(username));
            } else if (text.equals("Riwayat Transaksi")) {
                lblCurrentMenu.setText("Transaction History");
                showPanel(new HistoryView());
            } else if (text.equals("Log Aktivitas")) {
                lblCurrentMenu.setText("System Activity Logs");
                showPanel(new LogView());
            } else if (text.equals("Laporan")) {
                lblCurrentMenu.setText("Sales Reports");
                showPanel(new ReportView());
            } else if (text.equals("Pengaturan")) {
                lblCurrentMenu.setText("Account Settings");
                showPanel(new UserView());
            }
        });
        return btn;
    }

    private void toggleTheme(JButton btnTheme) {
        FlatAnimatedLafChange.showSnapshot();
        try {
            if (!isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnTheme.setText("☀️");
                isDarkMode = true;
            } else {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                btnTheme.setText("🌙");
                isDarkMode = false;
            }
            FlatLaf.updateUI();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPanel(JPanel panel) {
        try {
            for (Webcam webcam : Webcam.getWebcams()) {
                if (webcam.isOpen()) {
                    webcam.close();
                }
            }
        } catch (Exception e) {}

        mainContent.removeAll();
        mainContent.add(panel, BorderLayout.CENTER);
        mainContent.repaint();
        mainContent.revalidate();
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy | HH:mm:ss");
            lblClock.setText(sdf.format(new Date()));
        });
        timer.start();
    }
}