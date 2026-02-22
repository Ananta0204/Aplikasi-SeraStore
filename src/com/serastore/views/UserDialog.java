package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserDialog extends JDialog {
    
    private JTextField txtUser, txtNama;
    private JPasswordField txtPass;
    private JComboBox<String> cbRole;
    private UserView parent;
    private int idUser;

    public UserDialog(Frame owner, boolean modal, UserView parent, int id) {
        super(owner, modal);
        this.parent = parent;
        this.idUser = id;
        
        setTitle(id == -1 ? "Tambah Pengguna Baru" : "Edit Data Pengguna");
        setSize(480, 680); 
        setLocationRelativeTo(owner);
        setResizable(false);
        
        initUI();
        
        if (id != -1) loadDataLama();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        
        JPanel headerPnl = new JPanel(new BorderLayout());
        headerPnl.setBackground(Color.WHITE);
        headerPnl.setBorder(new EmptyBorder(30, 0, 15, 0));
        
        JLabel lblTitle = new JLabel(idUser == -1 ? "CREATE ACCOUNT" : "UPDATE ACCOUNT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(128, 0, 0)); 
        
        JLabel lblSub = new JLabel("Silahkan isi data karyawan di bawah ini", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        
        headerPnl.add(lblTitle, BorderLayout.NORTH);
        headerPnl.add(lblSub, BorderLayout.CENTER);
        
        add(headerPnl, BorderLayout.NORTH);
        
        JPanel mainPnl = new JPanel(new GridBagLayout());
        mainPnl.setBackground(Color.WHITE);
        mainPnl.setBorder(new EmptyBorder(10, 45, 40, 45));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        txtUser = createModernField("Masukkan Username...");
        txtNama = createModernField("Nama Lengkap Karyawan...");
        
        txtPass = new JPasswordField();
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password...");
        txtPass.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 20; height: 50; margin: 0,15,0,15; background: #F8F9FA; borderWidth: 0; focusWidth: 0; showRevealButton: true;"
        );
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        cbRole = new JComboBox<>(new String[]{"Admin", "Kasir"});
        cbRole.putClientProperty(FlatClientProperties.STYLE, "arc: 20; height: 50; background: #F8F9FA; borderWidth: 0; focusWidth: 0;");
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        addLabel(mainPnl, "USERNAME (ID LOGIN)", gbc, 0);
        addComponent(mainPnl, txtUser, gbc, 1);
        
        addLabel(mainPnl, "NAMA LENGKAP", gbc, 2);
        addComponent(mainPnl, txtNama, gbc, 3);
        
        addLabel(mainPnl, "KATA SANDI", gbc, 4);
        addComponent(mainPnl, txtPass, gbc, 5);
        
        addLabel(mainPnl, "JABATAN / ROLE", gbc, 6);
        addComponent(mainPnl, cbRole, gbc, 7);

        JButton btnSave = new JButton("SIMPAN DATA PENGGUNA");
        btnSave.setBackground(new Color(128, 0, 0));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Font Gede
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 20; height: 55; borderWidth: 0; hoverBackground: #A00000;");
        btnSave.addActionListener(e -> simpan());

        gbc.gridy = 8;
        gbc.insets = new Insets(40, 0, 0, 0);
        mainPnl.add(btnSave, gbc);

        add(mainPnl, BorderLayout.CENTER);
    }

    private JTextField createModernField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 20; height: 50; margin: 0,15,0,15; background: #F8F9FA; borderWidth: 0; focusWidth: 0; showClearButton: true;"
        );
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        return f;
    }

    private void addLabel(JPanel p, String text, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.insets = new Insets(20, 0, 8, 0); 
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.GRAY);
        p.add(l, gbc);
    }

    private void addComponent(JPanel p, JComponent c, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(c, gbc);
    }

    private void loadDataLama() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id_user=?");
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtUser.setText(rs.getString("username"));
                txtNama.setText(rs.getString("nama_lengkap"));
                txtPass.setText(rs.getString("password"));
                cbRole.setSelectedItem(rs.getString("role"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void simpan() {
        String user = txtUser.getText();
        String nama = txtNama.getText();
        String pass = new String(txtPass.getPassword());
        String role = cbRole.getSelectedItem().toString();

        if (user.isEmpty() || nama.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data wajib diisi!");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String sql = (idUser == -1) 
                ? "INSERT INTO users (username, nama_lengkap, password, role) VALUES (?,?,?,?)"
                : "UPDATE users SET username=?, nama_lengkap=?, password=?, role=? WHERE id_user=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, nama);
            ps.setString(3, pass);
            ps.setString(4, role);
            if (idUser != -1) ps.setInt(5, idUser);
            
            ps.executeUpdate();
            
            parent.loadData();
            dispose();
            JOptionPane.showMessageDialog(parent, "Data Pengguna Berhasil Disimpan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }
}