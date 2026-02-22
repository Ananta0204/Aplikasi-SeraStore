package com.serastore.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnection {
    private static Connection conn;
    
    public static Connection getConnection() {
        if (conn == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/serastore_db";
                String user = "root";
                String pass = "";
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                conn = DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Gagal Koneksi Ke Database: " + e.getMessage());
            }
        }
        return conn;
    }
}