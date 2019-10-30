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
	 *     firstName, lastName, username, password, email, login, permission
	 *         login is boolean
	 * unverifiedaccounts columns:
	 *     firstName, lastName, username, password, email, applicationDate
	 *         applicationDate inserted with CURDATE() function
	 *         server can periodically check for entries with an applicationDate
	 *         that is 2 days less than the current date and remove those entries
	 */
	
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
		if (user.length() > 20 || password.length() > 25) {
			return false;
		}
		
		ResultSet rs;
		try {
			rs = sqlHandler.callProcedure("Try_Login(?,?)", 2, new String[] {user, password});
		} catch (Exception e) {
			System.err.println("Exception in calling Try_Login(" + user + "," + password + ")");
			e.printStackTrace();
			return false;
		}

		try {
			if (rs.next()) {
				sqlHandler.callProcedure("Update_Login(?,?)", 2, new String[] {user, "1"});
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Exception in calling Update_Login(" + user + ",1)");
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
		try {
			ResultSet rs = sqlHandler.callProcedure("LoggedIn()");
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("Exception in calling LoggedIn()");
			e.printStackTrace();
		}
		return false;
	}
	/*
	 * Check if the user is an admin
	 */
	public boolean isAdmin() {
		try {
			ResultSet rs = sqlHandler.callProcedure("Get_Permission_Level()");
			/*rs.next();
			String permissionlvl = rs.getString("permission");
			System.out.println(permissionlvl);
			if(permissionlvl == "0") 
			{
				return true;
			}*/
			if(rs.next()) {
				int i = Integer.parseInt(rs.getString("permission"));
				if(i == 1) {
					return true;
				}
			}
		} catch (SQLException e) {
			System.err.println("Exception in calling Get_Permission_Level()");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Get the name of the currently logged in user
	 * 
	 * @author kemp
	 * @return String firstName + lastName
	 */
	public String loggedInDisplayName() {
		if (!this.isLoggedIn()) {
			return null;
		}
		
		try {
			ResultSet rs = sqlHandler.callProcedure("LoggedIn()");
			if(rs.next()) {
				return rs.getString("firstName") + " " + rs.getString("lastName");
			}
		} catch (Exception e) {
			System.err.println("Exception in calling LoggedIn()");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the username of the currently logged in user
	 * 
	 * @author kemp
	 * @return String username
	 */
	public String loggedInUsername() {
		if (!this.isLoggedIn()) {
			return null;
		}
		
		try {
			ResultSet rs = sqlHandler.callProcedure("LoggedIn()");
			if(rs.next()) {
				return rs.getString("username");
			}
		} catch (Exception e) {
			System.err.println("Exception in calling LoggedIn()");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the email of the currently logged in user
	 * 
	 * @author kemp
	 * @return String email
	 */
	public String loggedInEmail() {
		if (!this.isLoggedIn()) {
			return null;
		}
		
		try {
			ResultSet rs = sqlHandler.callProcedure("LoggedIn()");
			if(rs.next()) {
				return rs.getString("email");
			}
		} catch (Exception e) {
			System.err.println("Exception in calling LoggedIn()");
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
		try {
			ResultSet rs = sqlHandler.callProcedure("Email_Exists(?)", 1, new String[] {email});
			if(rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("Exception in calling Email_Exists(" + email + ")");
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
		try {
			ResultSet rs = sqlHandler.callProcedure("User_Exists(?)", 1, new String[] {user});
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Exception in calling User_Exists(" + user + ")");
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
	public boolean createNewUser(String fName, String lName, String user, String password, String email) {
		if (usernameExists(user)) {
			return false;
		} else if (emailExists(email)) {
			return false;
		} else {
			try {
				sqlHandler.callProcedure("Create_Unverified_User(?,?,?,?,?)", 5, new String[] {fName, lName, user, password, email});
			} catch (Exception e) {
				System.err.println("Exception in calling Create_Unverified_User(" + fName + "," + lName + "," + user + "," + password + "," + email + ")");
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
	
	/**
	 * Logout all users (should only be one)
	 */
	public void logout() {
		try {
			sqlHandler.callProcedure("Update_Login(?,?)", 2, new String[] {"", "0"});
		} catch (Exception e) {
			throw new RuntimeException("Exception in calling Update_Login('',0)");
		}
	}
}
