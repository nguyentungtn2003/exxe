package com.example.exe2update;

import java.sql.*;

public class TestSQL {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ Kết nối thành công!");
        } catch (SQLException e) {
            System.out.println("❌ Lỗi kết nối:");
            e.printStackTrace();
        }
    }
}
