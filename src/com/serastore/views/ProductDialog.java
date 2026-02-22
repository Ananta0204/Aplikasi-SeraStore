package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.serastore.database.DBConnection;
import com.serastore.utils.Logger;
import java.awt.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProductDialog extends JDialog implements Runnable {

    private JTextField txtBarcode, txtNama, txtHargaBeli, txtHargaJual, txtStok, txtSatuan;
    private JComboBox<String> cbKategori;
    private JLabel lblFotoPreview;
    private JPanel leftPanel; 
    
    private String pathFotoFinal = ""; 
    private Webcam webcam = null;
    private WebcamPanel webcamPanel = null;
    private Executor executor = Executors.newSingleThreadExecutor();
    private StockView parent;
    private boolean isRunning = true;
    private int idProduk = -1;
    private String currentUsername;

    private final int PREVIEW_SIZE = 170; 

    public ProductDialog(Frame owner, boolean modal, StockView parent, int id, String username) {
        super(owner, modal);
        this.parent = parent;
        this.idProduk = id;
        this.currentUsername = username;

        setTitle(id == -1 ? "Tambah Produk Baru" : "Edit Data Produk");
        setSize(1150, 750); 
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        initCamera();

        if (id != -1) loadDataLama();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                stopCamera();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());

        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(20, 20, 20));
        leftPanel.setPreferredSize(new Dimension(500, 0));
        
        JLabel lblCamTitle = new JLabel("SCANNER AREA", SwingConstants.CENTER);
        lblCamTitle.setForeground(new Color(200, 200, 200));
        lblCamTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCamTitle.setBorder(new EmptyBorder(20, 0, 20, 0));
        leftPanel.add(lblCamTitle, BorderLayout.NORTH);
        
        JLabel lblLoading = new JLabel("Menyiapkan Kamera...", SwingConstants.CENTER);
        lblLoading.setForeground(Color.GRAY);
        leftPanel.add(lblLoading, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(25, 40, 25, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        txtBarcode = createModernField("Scan / Ketik Barcode...");
        txtNama = createModernField("Nama Produk Lengkap...");
        
        cbKategori = new JComboBox<>(new String[]{"Umum", "Makanan", "Minuman", "Elektronik", "Fashion", "Obat", "Lainnya"});
        cbKategori.putClientProperty(FlatClientProperties.STYLE, "arc: 15; height: 45; background: #F8F9FA; border:0; focusWidth:0;");
        
        txtHargaBeli = createModernField("0");
        txtHargaJual = createModernField("0");
        txtStok = createModernField("0");
        txtSatuan = createModernField("Pcs / Box");

        addLabel(rightPanel, "KODE BARCODE", gbc, 0);
        addComponent(rightPanel, txtBarcode, gbc, 1);

        addLabel(rightPanel, "NAMA PRODUK", gbc, 2);
        addComponent(rightPanel, txtNama, gbc, 3);

        addLabel(rightPanel, "KATEGORI", gbc, 4);
        addComponent(rightPanel, cbKategori, gbc, 5);

        JPanel pPrice = new JPanel(new GridLayout(1, 2, 20, 0)); pPrice.setOpaque(false);
        JPanel pBeli = new JPanel(new BorderLayout(0, 5)); pBeli.setOpaque(false);
        pBeli.add(createLabel("HARGA BELI"), BorderLayout.NORTH); pBeli.add(txtHargaBeli, BorderLayout.CENTER);
        JPanel pJual = new JPanel(new BorderLayout(0, 5)); pJual.setOpaque(false);
        pJual.add(createLabel("HARGA JUAL"), BorderLayout.NORTH); pJual.add(txtHargaJual, BorderLayout.CENTER);
        pPrice.add(pBeli); pPrice.add(pJual);
        gbc.gridy = 6; gbc.insets = new Insets(15, 0, 5, 0); rightPanel.add(pPrice, gbc);

        JPanel pStock = new JPanel(new GridLayout(1, 2, 20, 0)); pStock.setOpaque(false);
        JPanel pSisa = new JPanel(new BorderLayout(0, 5)); pSisa.setOpaque(false);
        pSisa.add(createLabel("STOK"), BorderLayout.NORTH); pSisa.add(txtStok, BorderLayout.CENTER);
        JPanel pUnit = new JPanel(new BorderLayout(0, 5)); pUnit.setOpaque(false);
        pUnit.add(createLabel("SATUAN"), BorderLayout.NORTH); pUnit.add(txtSatuan, BorderLayout.CENTER);
        pStock.add(pSisa); pStock.add(pUnit);
        gbc.gridy = 7; rightPanel.add(pStock, gbc);

        JPanel bottomGroup = new JPanel(new BorderLayout(20, 0));
        bottomGroup.setOpaque(false);
        bottomGroup.setBorder(new EmptyBorder(25, 0, 0, 0));

        lblFotoPreview = new JLabel("FOTO", SwingConstants.CENTER);
        lblFotoPreview.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE)); 
        lblFotoPreview.setOpaque(true);
        lblFotoPreview.setBackground(new Color(250, 250, 250));
        lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        lblFotoPreview.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 15)); 
        btnPanel.setOpaque(false);

        JButton btnUpload = new JButton("Ganti / Upload Foto");
        btnUpload.setPreferredSize(new Dimension(0, 45));
        btnUpload.setBackground(new Color(245, 245, 245));
        btnUpload.setForeground(Color.BLACK);
        btnUpload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpload.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth:0; hoverBackground:#E0E0E0;");
        btnUpload.addActionListener(e -> pilihDanCropFoto());

        JButton btnSave = new JButton("SIMPAN DATA");
        btnSave.setPreferredSize(new Dimension(0, 55)); 
        btnSave.setBackground(new Color(128, 0, 0)); 
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth:0; hoverBackground:#A00000;");
        btnSave.addActionListener(e -> prosesSimpan());

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnUpload, BorderLayout.NORTH); 
        btnWrapper.add(Box.createVerticalGlue(), BorderLayout.CENTER); 
        btnWrapper.add(btnSave, BorderLayout.SOUTH);

        bottomGroup.add(lblFotoPreview, BorderLayout.WEST);
        bottomGroup.add(btnWrapper, BorderLayout.CENTER);

        gbc.gridy = 8;
        rightPanel.add(bottomGroup, gbc);

        add(rightPanel, BorderLayout.CENTER);
    }

    private JTextField createModernField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.putClientProperty(FlatClientProperties.STYLE, "arc: 15; height: 45; margin: 0,15,0,15; background: #F8F9FA; borderWidth: 0; focusWidth: 0; showClearButton: true;");
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return f;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Color.GRAY);
        return l;
    }

    private void addLabel(JPanel p, String text, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.insets = new Insets(15, 0, 5, 0); p.add(createLabel(text), gbc);
    }

    private void addComponent(JPanel p, JComponent c, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.insets = new Insets(0, 0, 0, 0); p.add(c, gbc);
    }

    private void initCamera() {
        executor.execute(() -> {
            try {
                for (Webcam c : Webcam.getWebcams()) { if (c.getName().contains("DroidCam")) { webcam = c; break; } }
                if (webcam == null && !Webcam.getWebcams().isEmpty()) webcam = Webcam.getDefault();

                if (webcam != null) {
                    if (webcam.isOpen()) webcam.close();
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    webcam.open();

                    SwingUtilities.invokeLater(() -> {
                        leftPanel.removeAll();
                        JLabel lblTitle = new JLabel("SCANNER ACTIVE", SwingConstants.CENTER);
                        lblTitle.setForeground(new Color(255, 215, 0));
                        lblTitle.setBorder(new EmptyBorder(15,0,15,0));
                        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        leftPanel.add(lblTitle, BorderLayout.NORTH);

                        webcamPanel = new WebcamPanel(webcam);
                        webcamPanel.setMirrored(true);
                        webcamPanel.setFillArea(true);
                        leftPanel.add(webcamPanel, BorderLayout.CENTER);
                        leftPanel.revalidate(); leftPanel.repaint();
                    });
                    run(); 
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    @Override public void run() {
        while (isRunning && webcam != null && webcam.isOpen()) {
            try {
                BufferedImage img = webcam.getImage();
                if (img == null) continue;
                Result res = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(img))));
                if (res != null) {
                    txtBarcode.setText(res.getText());
                    Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(2000);
                }
            } catch (Exception e) {}
        }
    }

    private void stopCamera() {
        isRunning = false;
        if (webcam != null && webcam.isOpen()) webcam.close();
    }

    private void loadDataLama() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE id_produk=?");
            ps.setInt(1, idProduk);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtBarcode.setText(rs.getString("barcode"));
                txtNama.setText(rs.getString("nama_produk"));
                
                int idKat = rs.getInt("id_kategori");
                if (idKat > 0 && idKat <= cbKategori.getItemCount()) cbKategori.setSelectedIndex(idKat - 1);
                
                txtHargaBeli.setText(String.format("%.0f", rs.getDouble("harga_beli")));
                txtHargaJual.setText(String.format("%.0f", rs.getDouble("harga_jual")));
                txtStok.setText(String.valueOf(rs.getInt("stok")));
                txtSatuan.setText(rs.getString("satuan"));
                pathFotoFinal = rs.getString("gambar_path");
                
                if (pathFotoFinal != null && !pathFotoFinal.isEmpty()) {
                    File f = new File(pathFotoFinal);
                    if(f.exists()){
                        ImageIcon icon = new ImageIcon(pathFotoFinal);
                        Image img = icon.getImage().getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_SMOOTH);
                        lblFotoPreview.setIcon(new ImageIcon(img));
                        lblFotoPreview.setText("");
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void pilihDanCropFoto() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                BufferedImage ori = ImageIO.read(file);
                
                int size = Math.min(ori.getWidth(), ori.getHeight());
                int x = (ori.getWidth() - size) / 2;
                int y = (ori.getHeight() - size) / 2;
                BufferedImage cropped = ori.getSubimage(x, y, size, size);

                lblFotoPreview.setIcon(new ImageIcon(cropped.getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_SMOOTH)));
                lblFotoPreview.setText("");

                File folder = new File("product_images");
                if (!folder.exists()) folder.mkdir();
                File dest = new File(folder, "IMG_" + System.currentTimeMillis() + ".jpg");
                ImageIO.write(cropped, "jpg", dest);
                pathFotoFinal = dest.getAbsolutePath();
                
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Gagal Crop: " + e.getMessage()); }
        }
    }

    private void prosesSimpan() {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = (idProduk == -1) 
                ? "INSERT INTO products (barcode, nama_produk, id_kategori, harga_beli, harga_jual, stok, satuan, gambar_path) VALUES (?,?,?,?,?,?,?,?)"
                : "UPDATE products SET barcode=?, nama_produk=?, id_kategori=?, harga_beli=?, harga_jual=?, stok=?, satuan=?, gambar_path=? WHERE id_produk=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtBarcode.getText());
            ps.setString(2, txtNama.getText());
            
            int index = cbKategori.getSelectedIndex(); 
            ps.setInt(3, index + 1); 

            ps.setDouble(4, Double.parseDouble(txtHargaBeli.getText()));
            ps.setDouble(5, Double.parseDouble(txtHargaJual.getText()));
            ps.setInt(6, Integer.parseInt(txtStok.getText()));
            ps.setString(7, txtSatuan.getText());
            ps.setString(8, pathFotoFinal);
            
            if (idProduk != -1) ps.setInt(9, idProduk);

            ps.executeUpdate();
            
            Logger.addLog(currentUsername, (idProduk == -1 ? "Tambah: " : "Edit: ") + txtNama.getText());
            
            if (parent != null) parent.loadData("");
            stopCamera();
            dispose();
            JOptionPane.showMessageDialog(null, "Berhasil Disimpan!");
            
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage()); 
        }
    }
}