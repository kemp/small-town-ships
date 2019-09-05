package me.smalltownships;

import java.sql.*;

public class MySQLHandler {

	public MySQLHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String performStatement(String sql) {
		try {
			Connection con = 
					DriverManager.getConnection("jdbc:mysql://localhost:3306/smalltownships?useSSL=false", 
							"root", "qwerty");
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			return rs.getString(3);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "unsuccessful";
	}

}
