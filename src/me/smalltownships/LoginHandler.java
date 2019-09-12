package me.smalltownships;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginHandler {
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find JDBC libraries", e);
		}
	}

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
		// TODO: Query database for an account with the given username and password
		// TODO: If the username and password match an account, set the login flag
		return true;
	}
	
	/**
	 * Check whether an account with the given email exists.
	 * 
	 * @param email The email address to check for
	 * @return True if an account has the email address "email"
	 */
	public boolean emailExists(String email) {
		// Checks within the verified accounts table, this can be changed if necessary
		String sql = "select * from smalltownships.verifiedaccounts where email='"+email+"';";
		ResultSet rs = sqlHandler.performStatement(sql);	
		try {
			if(rs.next())
			return true;
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
		// TODO: Query the database for an account with the given username
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
		// TODO: Check if an account with the given username and/or password exists
		// TODO: Add a new account to the DB with the given username, password and email address
		return false;
	}
}
