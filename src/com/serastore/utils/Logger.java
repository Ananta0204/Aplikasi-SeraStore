package com.serastore.utils;

import com.serastore.database.DBConnection;
import java.sql.PreparedStatement;

public class Logger {
    public static void addLog(String username, String aksi) {
        try {
            String sql = "INSERT INTO activity_logs (id_user, aksi) " +
                         "VALUES ((SELECT id_user FROM users WHERE username=?), ?)";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, aksi);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Gagal catat log: " + e.getMessage());
        }
    }
}