package me.smalltownships;

import java.sql.*;

public class MySQLHandler {
	
	Connection con;
	
	public MySQLHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/smalltownships?useSSL=false", 
					"root", "qwerty");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet performStatement(String sql) {
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			return stmt.executeQuery(sql);
			} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
}
