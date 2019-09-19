package me.smalltownships;

import java.sql.*;

public class MySQLHandler implements AutoCloseable {
	
	Connection con;
	
	public MySQLHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/smalltownships?useSSL=false&allowPublicKeyRetrieval=true", 
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
	 *  
	 *  @param SQL string to be executed
	 *  @return Result set of table that was queried
	 */
	public ResultSet queryTable(String sql) {
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
	 *  Function for issuing data manipulation statements to database
	 *  
	 *  @param String SQL statement that should return nothing
	 *  @return True if statement executed, false if an exception was thrown
	 */
	public boolean updateTable(String sql) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(sql);
			return true;
			} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Utility method, allowing for conversion of verifiedAccounts table to String
	 * 
	 * @param ResultSet from the verifiedAccounts table to be converted to string
	 * @return String representation of table
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
