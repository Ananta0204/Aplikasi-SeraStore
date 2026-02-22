package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import com.serastore.utils.Logger;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginView extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    public LoginView() {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        initComponent();
    }

    private void initComponent() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 0, 0), getWidth(), getHeight(), new Color(20, 0, 0));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        JLabel lblWelcome = new JLabel("<html><center><h1 style='color:white; font-size:50px;'>SERA STORE</h1>"
                + "<p style='color:#E0E0E0; font-size:16px;'>Smart Solution for Your Business</p></center></html>");
        leftPanel.add(lblWelcome);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        JPanel formBox = new JPanel(new GridBagLayout());
        formBox.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        formBox.add(lblTitle, gbc);

        JLabel lblSub = new JLabel("Please enter your details to sign in");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(Color.GRAY);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        formBox.add(lblSub, gbc);

        JLabel lblUserHeader = new JLabel("Username / ID Karyawan");
        lblUserHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserHeader.setForeground(new Color(128, 0, 0));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        formBox.add(lblUserHeader, gbc);

        txtUser = new JTextField();
        txtUser.setPreferredSize(new Dimension(400, 50));
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: serakasir01");
        txtUser.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #F8F9FA; focusColor: #800000; outlineColor: #E0E0E0;");
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        formBox.add(txtUser, gbc);

        JLabel lblPassHeader = new JLabel("Password");
        lblPassHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassHeader.setForeground(new Color(128, 0, 0));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        formBox.add(lblPassHeader, gbc);

        txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(400, 50));
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "••••••••");
        txtPass.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #F8F9FA; focusColor: #800000; outlineColor: #E0E0E0; showRevealButton: true;");
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 45, 0);
        formBox.add(txtPass, gbc);

        btnLogin = new JButton("SIGN IN TO SYSTEM");
        btnLogin.setPreferredSize(new Dimension(400, 55)); 
        btnLogin.setBackground(new Color(128, 0, 0));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 0; focusWidth: 0; hoverBackground: #A00000;");
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 15, 0);
        formBox.add(btnLogin, gbc);

        JButton btnExit = new JButton("Close and Shutdown Application");
        btnExit.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        btnExit.setForeground(new Color(180, 0, 0)); // MERAH GANTENG
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        formBox.add(btnExit, gbc);

        btnLogin.addActionListener(e -> prosesLogin());
        txtPass.addActionListener(e -> prosesLogin());
        btnExit.addActionListener(e -> System.exit(0));

        rightPanel.add(formBox);
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel);
    }

    private void prosesLogin() {
        String username = txtUser.getText();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan lengkapi data!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("nama_lengkap");
                String role = rs.getString("role");
                Logger.addLog(username, "Berhasil Login");
                this.dispose(); 
                new DashboardView(nama, role).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Akun tidak ditemukan!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System Error: " + ex.getMessage());
        }
    }
}