package me.smalltownships;

import java.sql.*;

public class MySQLHandler implements AutoCloseable {
	
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

	/**
	 *  Designed to be called by LoginHandler / Registration page
	 *  Returns a result set that can be manipulated
	 *  
	 *  TODO Secure this via 'protected' in package with classes that need access to it
	 */
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
	
	/**
	 * Utility method, allowing for conversion of verifiedAccounts table to String
	 */
	public String verifiedToString(ResultSet rs) {
		String s = "";
		try {
			int columnCount = rs.getMetaData().getColumnCount();
			while(rs.next()){
				s = s.concat(rs.getRow()+": ");
				for (int i = 1; i <= columnCount; i++) {
					String value = rs.getString(i);
					s = s.concat("["+value+"] ");
				}
				s = s.concat("\n");
			}
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		// Close the database connection
		con.close();
	}
	
}
