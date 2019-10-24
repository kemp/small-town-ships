package me.smalltownships;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginHandler {
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find JDBC libraries", e);
		}
	}
	
	/*
	 * Database Layout
	 * Tables: verifiedaccounts, unverifiedaccounts
	 * 
	 * verifiedaccounts columns:
	 *     firstName, lastName, username, password, email, login
	 *         login is boolean
	 * unverifiedaccounts columns:
	 *     firstName, lastName, username, password, email, applicationDate
	 *         applicationDate inserted with CURDATE() function
	 *         server can periodically check for entries with an applicationDate
	 *         that is 2 days less than the current date and remove those entries
	 */

	// FIXME: Need to know the database layout to write the queries
	
	private MySQLHandler sqlHandler;
	
	public LoginHandler() {
		sqlHandler = new MySQLHandler();
	}
	
	/**
	 * Attempt to login using the given username and password
	 * 
	 * @param user The username to search for
	 * @param password The password to search for
	 * @return True if the username and password match, false otherwise
	 */
	public boolean tryLogin(String user, String password) {
		ResultSet rs = sqlHandler.queryTable("select username, password from verifiedaccounts where username = '" + user + "' AND password = '" + password + "';");

		try {
			if (rs.next()) {
				sqlHandler.updateTable("update smalltownships.verifiedaccounts set login=1 where username = '" + user + "';");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check whether a user is currently logged in or not
	 * 
	 * @author kemp
	 * @return boolean whether a user is logged in
	 */
	public boolean isLoggedIn() {
		String sql = "select * from smalltownships.verifiedaccounts where login=1 limit 1;";
		ResultSet rs = sqlHandler.queryTable(sql);	
		try {
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	/*
	 * Check if the user is an admin
	 */
	public boolean isAdmin()
	{
		
		String sql = "select permission from smalltownships.verifiedaccounts where login=1 limit 1;";
		ResultSet rs = sqlHandler.queryTable(sql);
		
		try {
			/*rs.next();
			String permissionlvl = rs.getString("permission");
			System.out.println(permissionlvl);
			if(permissionlvl == "0") 
			{
				return true;
			}*/
			if(rs.next())
			{
				int i = Integer.parseInt(rs.getString("permission"));
				if(i == 0) 
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Get the name of the currently logged in user
	 * 
	 * @author kemp
	 * @return boolean whether a user is logged in
	 */
	public String loggedInUserName() {
		if (!this.isLoggedIn()) {
			return null;
		}
		
		String sql = "select firstName,lastName from smalltownships.verifiedaccounts where login=1 limit 1;";
		ResultSet rs = sqlHandler.queryTable(sql);	
		
		try {
			if(rs.next()) {
				return rs.getString("firstName") + " " + rs.getString("lastName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Check whether an account with the given email exists.
	 * 
	 * @param email The email address to check for
	 * @return True if an account has the email address "email"
	 */
	public boolean emailExists(String email) {
		// Checks within BOTH tables, this can be changed if necessary
		String sql = "select * from smalltownships.verifiedaccounts where email='"+email+"'"
				+ " union select * from smalltownships.unverifiedaccounts where email='"+email+"';";
		ResultSet rs = sqlHandler.queryTable(sql);	
		try {
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check whether an account with the given username exists.
	 * <p>
	 * May be redundant if the user's email address is used as their username.
	 * 
	 * @param user The username to check for
	 * @return True if an account has the username "user"
	 */
	public boolean usernameExists(String user) {
		String sql = "select * from smalltownships.verifiedaccounts where username='"+user+"'"
				+ " union select * from smalltownships.unverifiedaccounts where username='"+user+"';";
		ResultSet rs = sqlHandler.queryTable(sql);	
		try {
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Attempts to create a new account on the website.
	 * 
	 * @param user The username of the new account
	 * @param password The password of the new account
	 * @param email The email address of the new user (redundant if username is email address)
	 * @return True if a new account was created, false otherwise
	 */
	public boolean createNewUser(String user, String password, String email) {
		if (usernameExists(user)) {
			return false;
		} else if (emailExists(email)) {
			return false;
		} else {
			String sql = "INSERT INTO smalltownships.unverifiedaccounts (username, password, email, applicationDate) "
					+ "VALUES ('" + user + "', '"+password+"', '"+email+"', CURDATE());";
	    	return sqlHandler.updateTable(sql);
		}
	}
	
	/**
	 * Logout all users (should only be one)
	 */
	public void logout() {
		String sql = "update smalltownships.verifiedaccounts set login=0;";
		
		sqlHandler.updateTable(sql);
	}
}
