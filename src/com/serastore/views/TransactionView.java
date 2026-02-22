package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.awt.Dimension; 
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.Box; 
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Phrase;

public class TransactionView extends JPanel implements Runnable {

    private JTable tableCart;
    private DefaultTableModel tableModel;
    private JLabel lblTotal, lblNamaProduk, lblHargaProduk, lblGambarProduk, lblKembalian;
    private JTextField txtCash, txtDiskonGlobal, txtManualBarcode;
    private JPanel scannerBox;
    private Webcam webcam = null;
    private WebcamPanel webcamPanel = null;
    private Executor executor = Executors.newSingleThreadExecutor();
    private boolean isRunning = true;
    private double grandTotal = 0;

    public TransactionView() {
        setLayout(new BorderLayout(25, 25));
        setOpaque(false);
        initUI();
        initWebcam();
    }

    private void initUI() {
        JPanel cartPanel = new JPanel(new BorderLayout(0, 10));
        cartPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("KERANJANG BELANJA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        tableModel = new DefaultTableModel(new Object[][]{}, new String[]{"No", "Barcode", "Nama Barang", "Harga", "Qty", "Diskon", "Subtotal"}) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4 || c == 5; }
        };
        
        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (e.getColumn() == 4 || e.getColumn() == 5) recalcRow(row);
            }
        });
        
        tableCart = new JTable(tableModel);
        tableCart.setRowHeight(45); 
        tableCart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableCart.setShowVerticalLines(false);
        tableCart.setIntercellSpacing(new Dimension(0, 0));
        
        JScrollPane scroll = new JScrollPane(tableCart);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:20; border:0;");
        
        cartPanel.add(lblTitle, BorderLayout.NORTH);
        cartPanel.add(scroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(420, 0));
        rightPanel.setOpaque(false);

        scannerBox = new JPanel(new BorderLayout());
        scannerBox.setPreferredSize(new Dimension(420, 315)); 
        scannerBox.setMaximumSize(new Dimension(420, 315)); 
        scannerBox.setBackground(Color.BLACK);
        scannerBox.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scannerBox.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        txtManualBarcode = new JTextField();
        txtManualBarcode.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ketik Barcode & ENTER...");
        txtManualBarcode.putClientProperty(FlatClientProperties.STYLE, "arc: 20; margin:0,10,0,10; focusColor:#800000; borderColor:#E0E0E0;");
        txtManualBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtManualBarcode.setMaximumSize(new Dimension(420, 45));
        txtManualBarcode.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtManualBarcode.addActionListener(e -> {
            tambahKeKeranjang(txtManualBarcode.getText());
            txtManualBarcode.setText("");
            txtManualBarcode.requestFocus();
        });

        JButton btnReset = new JButton("RESET TRANSAKSI (BARU)");
        btnReset.setMaximumSize(new Dimension(420, 45)); 
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setBackground(new Color(245, 245, 245));
        btnReset.setForeground(Color.DARK_GRAY);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 0; hoverBackground: #dcdcdc;");
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Mulai transaksi baru?", "Konfirmasi", 0) == 0) resetUI();
        });

        JPanel detailBox = new JPanel(new BorderLayout(15, 15));
        detailBox.setBackground(Color.WHITE);
        detailBox.setMaximumSize(new Dimension(420, 160));
        detailBox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        detailBox.putClientProperty(FlatClientProperties.STYLE, "arc:20;");
        detailBox.setAlignmentX(Component.CENTER_ALIGNMENT); // Biar lurus

        lblGambarProduk = new JLabel("NO IMG", SwingConstants.CENTER);
        lblGambarProduk.setPreferredSize(new Dimension(110, 110));
        lblGambarProduk.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
        lblGambarProduk.setForeground(Color.LIGHT_GRAY);

        JPanel infoBarang = new JPanel(new GridLayout(2, 1));
        infoBarang.setOpaque(false);
        lblNamaProduk = new JLabel("-");
        lblNamaProduk.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        lblHargaProduk = new JLabel("Rp 0");
        lblHargaProduk.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHargaProduk.setForeground(new Color(128, 0, 0)); 
        
        infoBarang.add(lblNamaProduk);
        infoBarang.add(lblHargaProduk);

        detailBox.add(lblGambarProduk, BorderLayout.WEST);
        detailBox.add(infoBarang, BorderLayout.CENTER);

        rightPanel.add(scannerBox);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(txtManualBarcode);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
        rightPanel.add(btnReset);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
        rightPanel.add(detailBox);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(0, 130));
        bottomPanel.setBackground(new Color(110, 0, 0)); 
        bottomPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;");
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JPanel totalInfo = new JPanel(new GridLayout(2, 1));
        totalInfo.setOpaque(false);
        lblTotal = new JLabel("TOTAL: Rp 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTotal.setForeground(Color.WHITE);
        lblKembalian = new JLabel("KEMBALIAN: Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblKembalian.setForeground(new Color(255, 255, 255, 180));
        totalInfo.add(lblTotal);
        totalInfo.add(lblKembalian);

        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        payPanel.setOpaque(false);

        txtDiskonGlobal = createSemiRoundedField("Diskon (Rp)");
        txtDiskonGlobal.setPreferredSize(new Dimension(140, 50));
        
        txtCash = createSemiRoundedField("Bayar (Rp)");
        txtCash.setPreferredSize(new Dimension(200, 50));
        txtCash.setFont(new Font("Segoe UI", Font.BOLD, 20));

        txtDiskonGlobal.getDocument().addDocumentListener(new SimpleDocListener() { @Override void update() { hitungTotal(); } });
        txtCash.getDocument().addDocumentListener(new SimpleDocListener() { @Override void update() { hitungKembalian(); } });

        JButton btnPay = new JButton("BAYAR SEKARANG");
        btnPay.setPreferredSize(new Dimension(200, 50));
        btnPay.setBackground(Color.WHITE);
        btnPay.setForeground(new Color(110, 0, 0)); 
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPay.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 0; hoverBackground: #f2f2f2;");
        btnPay.addActionListener(e -> prosesBayar());

        payPanel.add(txtDiskonGlobal);
        payPanel.add(txtCash);
        payPanel.add(btnPay);

        bottomPanel.add(totalInfo, BorderLayout.WEST);
        bottomPanel.add(payPanel, BorderLayout.EAST);

        add(cartPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JTextField createSemiRoundedField(String hint) {
        JTextField f = new JTextField();
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
        f.putClientProperty(FlatClientProperties.STYLE, "arc: 20; margin:0,15,0,15; borderWidth:0; focusWidth:0; background:#FFFFFF;");
        return f;
    }

    private void showReceiptDialog(String noFaktur, double bayar) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Struk Transaksi", true);
        dialog.setSize(400, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextPane area = new JTextPane();
        area.setEditable(false);
        area.setBackground(new Color(250, 250, 250));
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        StyledDocument doc = area.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== SERA STORE ===\n");
        sb.append("Jalan Teknologi No. 88\n");
        sb.append("------------------------------------------\n");
        sb.append("No Faktur : ").append(noFaktur).append("\n");
        sb.append("Tanggal   : ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())).append("\n");
        sb.append("------------------------------------------\n\n");
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nama = tableModel.getValueAt(i, 2).toString();
            if(nama.length() > 25) nama = nama.substring(0, 22) + "...";
            String qty = tableModel.getValueAt(i, 4).toString();
            String price = String.format("%,.0f", Double.parseDouble(tableModel.getValueAt(i, 3).toString()));
            String sub = String.format("%,.0f", (double)tableModel.getValueAt(i, 6));
            sb.append(nama).append("\n");
            sb.append(qty).append(" x ").append(price).append(" = ").append(sub).append("\n");
        }
        
        sb.append("\n------------------------------------------\n");
        sb.append("TOTAL     : ").append(String.format("%,.0f", grandTotal)).append("\n");
        sb.append("BAYAR     : ").append(String.format("%,.0f", bayar)).append("\n");
        sb.append("KEMBALI   : ").append(String.format("%,.0f", bayar - grandTotal)).append("\n");
        sb.append("------------------------------------------\n");
        sb.append("\nTerima Kasih Telah Berbelanja!");

        area.setText(sb.toString());
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new GridLayout(1, 2, 10, 10));
        pnlBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        pnlBtn.setBackground(Color.WHITE);
        JButton btnPdf = new JButton("SIMPAN PDF");
        JButton btnOk = new JButton("TUTUP");
        btnPdf.setBackground(new Color(128, 0, 0));
        btnPdf.setForeground(Color.WHITE);
        btnPdf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPdf.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth:0; hoverBackground:#A00000;");
        btnOk.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
        btnPdf.addActionListener(e -> cetakStrukPDF(noFaktur, bayar));
        btnOk.addActionListener(e -> dialog.dispose());
        pnlBtn.add(btnPdf); pnlBtn.add(btnOk);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void cetakStrukPDF(String noFaktur, double bayar) {
        Document doc = new Document(new Rectangle(250, 800), 10, 10, 10, 10);
        try {
            String path = "struk_" + noFaktur + ".pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();
            com.lowagie.text.Font fHead = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font fSub = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font fBody = new com.lowagie.text.Font(com.lowagie.text.Font.COURIER, 10, com.lowagie.text.Font.NORMAL);

            Paragraph p1 = new Paragraph("SERA STORE", fHead); p1.setAlignment(Element.ALIGN_CENTER); doc.add(p1);
            Paragraph p2 = new Paragraph("Jl. Teknologi No.88\n", fSub); p2.setAlignment(Element.ALIGN_CENTER); doc.add(p2);
            Paragraph p3 = new Paragraph("--------------------------------------", fBody); p3.setAlignment(Element.ALIGN_CENTER); doc.add(p3);
            Paragraph pInfo = new Paragraph("No: " + noFaktur + "\nTgl: " + new java.util.Date().toLocaleString(), fSub);
            pInfo.setAlignment(Element.ALIGN_CENTER); doc.add(pInfo);
            doc.add(p3);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 1}); 
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String nama = tableModel.getValueAt(i, 2).toString();
                String detail = tableModel.getValueAt(i, 4) + " x " + String.format("%,.0f", Double.parseDouble(tableModel.getValueAt(i, 3).toString()));
                String sub = String.format("%,.0f", (double)tableModel.getValueAt(i, 6));
                PdfPCell cName = new PdfPCell(new Phrase(nama, fBody)); cName.setBorder(Rectangle.NO_BORDER); cName.setColspan(2); table.addCell(cName);
                PdfPCell cDet = new PdfPCell(new Phrase(detail, fSub)); cDet.setBorder(Rectangle.NO_BORDER); table.addCell(cDet);
                PdfPCell cPrice = new PdfPCell(new Phrase(sub, fBody)); cPrice.setBorder(Rectangle.NO_BORDER); cPrice.setHorizontalAlignment(Element.ALIGN_RIGHT); table.addCell(cPrice);
            }
            doc.add(table);
            doc.add(p3);

            PdfPTable tTotal = new PdfPTable(2);
            tTotal.setWidthPercentage(100);
            addFooterRow(tTotal, "Total", grandTotal, fBody);
            addFooterRow(tTotal, "Bayar", bayar, fBody);
            addFooterRow(tTotal, "Kembali", bayar - grandTotal, fBody);
            doc.add(tTotal);
            doc.add(p3);
            Paragraph pThanks = new Paragraph("Terima Kasih!", fHead); pThanks.setAlignment(Element.ALIGN_CENTER); doc.add(pThanks);
            doc.close();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(path));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addFooterRow(PdfPTable table, String label, double value, com.lowagie.text.Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font)); c1.setBorder(Rectangle.NO_BORDER); table.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(String.format("%,.0f", value), font)); c2.setBorder(Rectangle.NO_BORDER); c2.setHorizontalAlignment(Element.ALIGN_RIGHT); table.addCell(c2);
    }

    private void prosesBayar() {
        if (tableModel.getRowCount() == 0) return;
        try {
            double bayar = Double.parseDouble(txtCash.getText().replace(".", ""));
            if (bayar < grandTotal) { JOptionPane.showMessageDialog(this, "Uang Kurang!"); return; }
            String noFaktur = "TRX-" + System.currentTimeMillis();
            Connection conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement psH = conn.prepareStatement("INSERT INTO transactions (no_faktur, total_akhir, bayar, kembali) VALUES (?,?,?,?)");
            psH.setString(1, noFaktur); psH.setDouble(2, grandTotal); psH.setDouble(3, bayar); psH.setDouble(4, bayar - grandTotal);
            psH.executeUpdate();
            PreparedStatement psD = conn.prepareStatement("INSERT INTO transaction_details (no_faktur, id_produk, qty, subtotal) VALUES (?,?,?,?)");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String bc = tableModel.getValueAt(i, 1).toString();
                int qty = (int) tableModel.getValueAt(i, 4);
                double sub = (double) tableModel.getValueAt(i, 6);
                ResultSet rs = conn.createStatement().executeQuery("SELECT id_produk FROM products WHERE barcode='" + bc + "'");
                if (rs.next()) {
                    psD.setString(1, noFaktur); psD.setInt(2, rs.getInt(1)); psD.setInt(3, qty); psD.setDouble(4, sub);
                    psD.executeUpdate();
                    conn.createStatement().executeUpdate("UPDATE products SET stok = stok - " + qty + " WHERE barcode='" + bc + "'");
                }
            }
            conn.commit();
            showReceiptDialog(noFaktur, bayar);
            resetUI();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Cek Inputan!"); e.printStackTrace(); }
    }

    private void resetUI() {
        tableModel.setRowCount(0); grandTotal = 0; txtCash.setText(""); txtDiskonGlobal.setText("");
        lblTotal.setText("TOTAL: Rp 0"); lblKembalian.setText("KEMBALIAN: Rp 0");
        lblNamaProduk.setText("-"); lblHargaProduk.setText("Rp 0"); lblGambarProduk.setIcon(null);
    }

    private void tambahKeKeranjang(String barcode) {
        if(barcode.isEmpty()) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE barcode = ?");
            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nama = rs.getString("nama_produk");
                double harga = rs.getDouble("harga_jual");
                String path = rs.getString("gambar_path");
                lblNamaProduk.setText(nama); lblHargaProduk.setText("Rp " + String.format("%,.0f", harga));
                updatePreviewGambar(path);
                boolean ada = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1).toString().equals(barcode)) {
                        int qty = (int) tableModel.getValueAt(i, 4) + 1;
                        tableModel.setValueAt(qty, i, 4);
                        recalcRow(i);
                        ada = true; break;
                    }
                }
                if (!ada) tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, barcode, nama, harga, 1, 0.0, harga});
                hitungTotal();
            } else { Toolkit.getDefaultToolkit().beep(); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void recalcRow(int row) {
        try {
            double harga = Double.parseDouble(tableModel.getValueAt(row, 3).toString());
            int qty = Integer.parseInt(tableModel.getValueAt(row, 4).toString());
            double diskon = Double.parseDouble(tableModel.getValueAt(row, 5).toString());
            tableModel.setValueAt((harga * qty) - diskon, row, 6);
            hitungTotal();
        } catch (Exception e) {}
    }

    private void hitungTotal() {
        double sub = 0; for(int i=0; i<tableModel.getRowCount(); i++) sub += (double) tableModel.getValueAt(i, 6);
        double disc = 0; try { String d = txtDiskonGlobal.getText().replace(".", ""); if(!d.isEmpty()) disc = Double.parseDouble(d); } catch(Exception e){}
        grandTotal = sub - disc; if(grandTotal < 0) grandTotal = 0;
        lblTotal.setText("TOTAL: Rp " + String.format("%,.0f", grandTotal));
        hitungKembalian();
    }

    private void hitungKembalian() {
        try {
            double byr = Double.parseDouble(txtCash.getText().replace(".", ""));
            double kbl = byr - grandTotal;
            lblKembalian.setText(kbl < 0 ? "KURANG: Rp " + String.format("%,.0f", Math.abs(kbl)) : "KEMBALIAN: Rp " + String.format("%,.0f", kbl));
        } catch(Exception e) { lblKembalian.setText("KEMBALIAN: Rp 0"); }
    }

    private void updatePreviewGambar(String path) {
        if (path == null || path.isEmpty()) { lblGambarProduk.setIcon(null); lblGambarProduk.setText("NO IMG"); return; }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
            lblGambarProduk.setIcon(new ImageIcon(img));
            lblGambarProduk.setText("");
        } catch (Exception e) { lblGambarProduk.setText("ERR"); }
    }

    private void initWebcam() {
        executor.execute(() -> {
            try {
                for (Webcam c : Webcam.getWebcams()) if (c.getName().contains("DroidCam")) { webcam = c; break; }
                if (webcam == null) webcam = Webcam.getDefault();
                if (webcam != null) {
                    if (webcam.isOpen()) webcam.close();
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    webcam.open();
                    SwingUtilities.invokeLater(() -> {
                        webcamPanel = new WebcamPanel(webcam);
                        webcamPanel.setMirrored(true);
                        webcamPanel.setFillArea(true);
                        scannerBox.removeAll(); scannerBox.add(webcamPanel, BorderLayout.CENTER);
                        scannerBox.revalidate(); scannerBox.repaint();
                    });
                    run(); 
                }
            } catch (Exception e) { }
        });
    }

    @Override public void run() {
        while (isRunning && webcam != null && webcam.isOpen()) {
            try {
                BufferedImage img = webcam.getImage();
                if (img == null) continue;
                Result res = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(img))));
                if (res != null) { tambahKeKeranjang(res.getText()); Toolkit.getDefaultToolkit().beep(); Thread.sleep(2000); }
            } catch (Exception e) { }
        }
    }

    abstract class SimpleDocListener implements DocumentListener {
        abstract void update();
        @Override public void insertUpdate(DocumentEvent e) { update(); }
        @Override public void removeUpdate(DocumentEvent e) { update(); }
        @Override public void changedUpdate(DocumentEvent e) { update(); }
    }
}