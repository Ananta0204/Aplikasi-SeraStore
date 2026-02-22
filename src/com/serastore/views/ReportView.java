package com.serastore.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.serastore.database.DBConnection;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Desktop;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Document;

public class ReportView extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtTglMulai, txtTglSelesai;
    private JLabel lblTotalOmzet, lblTotalTrx;
    private double totalOmzet = 0;

    public ReportView() {
        setLayout(new BorderLayout(0, 25));
        setOpaque(false);
        initUI();
        loadData("SELECT * FROM transactions WHERE DATE(tgl_transaksi) = CURDATE() ORDER BY id_transaksi DESC");
    }

private void initUI() {
    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setBackground(Color.WHITE);
    topBar.putClientProperty(FlatClientProperties.STYLE, "arc: 25;"); 
    topBar.setBorder(new EmptyBorder(15, 25, 15, 25));

    JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
    leftFilter.setOpaque(false);

    JLabel lblDari = new JLabel("Dari:");
    lblDari.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lblDari.setForeground(Color.DARK_GRAY);

    JLabel lblSampai = new JLabel("Sampai:");
    lblSampai.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lblSampai.setForeground(Color.DARK_GRAY);

    txtTglMulai = createStyledField("YYYY-MM-DD");
    txtTglSelesai = createStyledField("YYYY-MM-DD");
    
    JButton btnFilter = new JButton("Saring Laporan");
    btnFilter.setBackground(new Color(128, 0, 0)); 
    btnFilter.setForeground(Color.WHITE);
    btnFilter.setPreferredSize(new Dimension(150, 40));
    btnFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; focusWidth: 0; font: bold;");

    leftFilter.add(lblDari);
    leftFilter.add(txtTglMulai);
    leftFilter.add(lblSampai);
    leftFilter.add(txtTglSelesai);
    leftFilter.add(btnFilter);

    JButton btnExport = new JButton("Export PDF");
    btnExport.setPreferredSize(new Dimension(130, 40));
    btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnExport.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #fdf2f2; foreground: #800000; borderWidth: 1; outlineColor: #800000;");

    topBar.add(leftFilter, BorderLayout.WEST);
    topBar.add(btnExport, BorderLayout.EAST);

        JPanel cardPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        cardPanel.setOpaque(false);

        lblTotalOmzet = new JLabel("Rp 0");
        lblTotalTrx = new JLabel("0 Transaksi");

        cardPanel.add(createReportCard("TOTAL PENDAPATAN", lblTotalOmzet, new Color(128, 0, 0)));
        cardPanel.add(createReportCard("JUMLAH TRANSAKSI", lblTotalTrx, new Color(44, 62, 80)));

        tableModel = new DefaultTableModel(new String[]{"No Faktur", "Tanggal", "Total Belanja", "Bayar", "Kembali"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");
        scroll.setBorder(BorderFactory.createEmptyBorder());

        btnFilter.addActionListener(e -> {
            String tgl1 = txtTglMulai.getText().trim();
            String tgl2 = txtTglSelesai.getText().trim();
            if(tgl1.isEmpty() || tgl2.isEmpty() || tgl1.equals("YYYY-MM-DD")) {
                JOptionPane.showMessageDialog(this, "Isi range tanggal dulu cuy!");
                return;
            }
            loadData("SELECT * FROM transactions WHERE DATE(tgl_transaksi) BETWEEN '" + tgl1 + "' AND '" + tgl2 + "' ORDER BY id_transaksi DESC");
        });

        btnExport.addActionListener(e -> exportLaporanPDF());

        JPanel headerSection = new JPanel(new BorderLayout(0, 20));
        headerSection.setOpaque(false);
        headerSection.add(topBar, BorderLayout.NORTH);
        headerSection.add(cardPanel, BorderLayout.CENTER);

        add(headerSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

private JTextField createStyledField(String placeholder) {
    JTextField f = new JTextField();
    f.setPreferredSize(new Dimension(150, 40));
    f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
    
    f.putClientProperty(FlatClientProperties.STYLE, 
        "arc: 12;" +
        "background: #F8F9FA;" +
        "margin: 0,10,0,10;" + 
        "focusColor: #800000;" +
        "showClearButton: true;"
    );
    return f;
}

    private JPanel createReportCard(String title, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.putClientProperty(FlatClientProperties.STYLE, "arc: 25;");
        p.setBorder(new EmptyBorder(20, 25, 20, 25));
        p.setPreferredSize(new Dimension(0, 110));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(color);

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private void loadData(String sql) {
        tableModel.setRowCount(0);
        totalOmzet = 0;
        int count = 0;
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                double total = rs.getDouble("total_akhir");
                totalOmzet += total;
                count++;
                tableModel.addRow(new Object[]{
                    rs.getString("no_faktur"),
                    rs.getString("tgl_transaksi"),
                    "Rp " + String.format("%,.0f", total),
                    "Rp " + String.format("%,.0f", rs.getDouble("bayar")),
                    "Rp " + String.format("%,.0f", rs.getDouble("kembali"))
                });
            }
            lblTotalOmzet.setText("Rp " + String.format("%,.0f", totalOmzet));
            lblTotalTrx.setText(count + " Transaksi");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void exportLaporanPDF() {
        if(tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Gak ada data buat di-export!");
            return;
        }
        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        try {
            String path = "Laporan_Penjualan_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            com.lowagie.text.Font fTitle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            doc.add(new Paragraph("LAPORAN PENJUALAN - SERA STORE", fTitle));
            doc.add(new Paragraph("Periode Filter: " + txtTglMulai.getText() + " s/d " + txtTglSelesai.getText()));
            doc.add(new Paragraph("Dicetak pada: " + new java.util.Date().toString()));
            doc.add(new Paragraph("\n"));

            PdfPTable pdfTable = new PdfPTable(5);
            pdfTable.setWidthPercentage(100);
            pdfTable.addCell("No Faktur");
            pdfTable.addCell("Tanggal");
            pdfTable.addCell("Total");
            pdfTable.addCell("Bayar");
            pdfTable.addCell("Kembali");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < 5; j++) {
                    pdfTable.addCell(tableModel.getValueAt(i, j).toString());
                }
            }

            doc.add(pdfTable);
            doc.add(new Paragraph("\nTOTAL OMZET: Rp " + String.format("%,.0f", totalOmzet)));
            doc.close();
            
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(path));
            JOptionPane.showMessageDialog(this, "Laporan PDF Berhasil Dibuat!");
        } catch (Exception e) { e.printStackTrace(); }
    }
}