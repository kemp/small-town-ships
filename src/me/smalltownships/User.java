package me.smalltownships;

import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class User extends InteractsWithSQL {

	/*
	 * Database Layout
	 * Tables: verifiedaccounts, unverifiedaccounts
	 *
	 * verifiedaccounts columns:
	 *     firstName, lastName, username, password, email, login, permission
	 *         login is a randomly generated token at login
	 * unverifiedaccounts columns:
	 *     firstName, lastName, username, password, email, applicationDate
	 *         applicationDate inserted with CURDATE() function
	 *         server can periodically check for entries with an applicationDate
	 *         that is 2 days less than the current date and remove those entries
	 */

	private static final int ADMIN_PERMISSION_LEVEL = 1;
	
	protected static final String USER_SESSION = "user";

	private String username, firstName, lastName, email;

    private int loginToken, permission;

	private User(String username, String firstName, String lastName, String email, int loginToken, int permission) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.loginToken = loginToken;
		this.permission = permission;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return firstName + " " + lastName;
	}

	public String getEmail() {
		return email;
	}

	public void removeLoginToken() {
		try {
			sqlHandler.callProcedure("Logout_User(?)", this.username);
		} catch (Exception e) {
			throw new RuntimeException("Exception in calling Logout_User(" + this.username + ")");
		}
	}

	public boolean isAdmin() {
        return permission == ADMIN_PERMISSION_LEVEL;
	}

	/**
	 * Attempt to login using the given username and password
	 * 
	 * @param username The username to search for
	 * @param password The password to search for
	 * @return The associated user, if login is successful
	 */
	public static User tryLogin(String username, String password, HttpSession session) {
		if (username.length() > 20 || password.length() > 25) {
			return null;
		}

		// Generate a random token to authenticate this user.
		int loginToken = (new Random()).nextInt();
		String hash;
		ResultSet rs;
		
		// get the password so that we can hash and verify it
		try {
			rs = sqlHandler.callProcedure("Verified_Password(?)", username);
			
			if (!rs.next()) {
				return null; 
			}
			hash = rs.getString("password");
		} catch (Exception e) {
			System.err.println("Exception in calling Verified_Password(" + username + ")");
			e.printStackTrace();
			return null;
		}
		
		if (!Encryption.verifyPassword(password, hash)) {
			// password does not match
			return null;
		}

		try {
			rs = sqlHandler.callProcedure("Try_Login(?,?,?)", username, hash, loginToken);

			if (rs.next()) {
				User user = fetchUserByToken(loginToken);

				// Set the currently logged in user on the session
				session.setAttribute(USER_SESSION, user);

                return user;
			} else {
				// The username and/or password were incorrect
				return null;
			}
		} catch (Exception e) {
			System.err.println("Exception in calling Try_Login(" + username + "," + password + ")");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return Updates the user from the database
	 */
	protected User fresh() {
		return fetchUserByToken(this.loginToken);
	}

	/**
	 * Logout the currently authenticated user
	 */
	public void logout(HttpSession session) {
	    this.removeLoginToken();

		session.removeAttribute(USER_SESSION);
	}

	/**
	 * Attempt to fetch the user by a given token.
	 *
	 * @param loginToken The currently logged in token
	 * @return a user
	 */
	private static User fetchUserByToken(int loginToken) {
		try {
			ResultSet rs = sqlHandler.callProcedure("User_With_Token(?)", loginToken);

			if(rs.next()) {
				return new User(
						rs.getString("username"),
						rs.getString("firstName"),
						rs.getString("lastName"),
						rs.getString("email"),
						loginToken,
						rs.getInt("permission")
				);
			}
		} catch (Exception e) {
			System.err.println("Exception in calling User_With_Token()");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Check whether a user is currently logged in or not
	 * 
	 * @return boolean whether a user is logged in
	 */
	public static boolean isLoggedIn(HttpSession session) {
		return loggedInUser(session) != null;
	}

	/**
	 * Get the currently logged in user, if there is one
	 *
	 * @return the user | null
	 */
	public static User loggedInUser(HttpSession session) {
		return (User)session.getAttribute(USER_SESSION);
	}

	/**
	 * Check whether an account with the given email exists.
	 * 
	 * @param email The email address to check for
	 * @return True if an account has the email address "email"
	 */
	public static boolean emailExists(String email) {
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
	 *
	 * May be redundant if the user's email address is used as their username.
	 * 
	 * @param user The username to check for
	 * @return True if an account has the username "user"
	 */
	public static boolean usernameExists(String user) {
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
	public static boolean createNewUser(String fName, String lName, String user, String password, String email) {
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

}
