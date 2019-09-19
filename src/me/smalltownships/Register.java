package me.smalltownships;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String SQL_UNVERIFIED_USER_EXISTS = "SELECT COUNT(username) FROM "
			+ "smalltownships.unverifiedaccounts WHERE username = ?;";
	private static final String SQL_VERIFIED_USER_EXISTS = "SELECT COUNT(username) FROM "
			+ "smalltownships.verifiedaccounts WHERE username = ?;";
	private static final String SQL_UNVERIFIED_EMAIL_EXISTS = "SELECT COUNT(email) FROM "
			+ "smalltownships.unverifiedaccounts WHERE email = ?;";
	private static final String SQL_VERIFIED_EMAIL_EXISTS = "SELECT COUNT(username) FROM "
			+ "smalltownships.verifiedaccounts WHERE username = ?;";
	private static final String SQL_NEW_USER = "INSERT INTO "
			+ "smalltownships.unverifiedaccounts VALUES (?, ?, ?, ?, ?, CURDATE());";
	private static final String SQL_GET_UNVERIFIED_DATE = "SELECT applicationDate FROM "
			+ "smalltownships.unverifiedaccounts WHERE username = ?;";
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find JDBC libraries", e);
		}
	}
	
	private boolean usernameExists(Connection con, String user) throws SQLException {
		PreparedStatement stmt;
		ResultSet rs;
		int cnt;
		// Check for matches in the verified users
		stmt = con.prepareStatement(SQL_VERIFIED_USER_EXISTS);
		stmt.setString(1, user);
		rs = stmt.executeQuery();
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		stmt.close();
		if (cnt != 0) {
			// Match found
			return true;
		}
		// Check for matches in the unverified users
		stmt = con.prepareStatement(SQL_UNVERIFIED_USER_EXISTS);
		stmt.setString(1, user);
		rs = stmt.executeQuery();
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		stmt.close();
		return (cnt != 0);	// Return if a match was found
	}

	private boolean emailExists(Connection con, String email) throws SQLException {
		PreparedStatement stmt;
		ResultSet rs;
		int cnt;
		// Check for matches in the verified users
		stmt = con.prepareStatement(SQL_VERIFIED_EMAIL_EXISTS);
		stmt.setString(1, email);
		rs = stmt.executeQuery();
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		stmt.close();
		if (cnt != 0) {
			// Match found
			return true;
		}
		// Check for matches in the unverified users
		stmt = con.prepareStatement(SQL_UNVERIFIED_EMAIL_EXISTS);
		stmt.setString(1, email);
		rs = stmt.executeQuery();
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		stmt.close();
		return (cnt != 0);	// Return if a match was found
	}
	
	private void createNewAccount(Connection con, String firstName, String lastName,
			String username, String password, String email) throws SQLException {
		PreparedStatement stmt;
		Date date;
		stmt = con.prepareStatement(SQL_NEW_USER);
		stmt.setString(1, firstName);
		stmt.setString(2, lastName);
		stmt.setString(3, username);
		stmt.setString(4, password);
		stmt.setString(5, email);
		stmt.executeUpdate();
		stmt.close();
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String firstName, lastName, username, password, confirmPassword, email;
		
		try (Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/smalltownships?useSSL=false",
				"root", "qwerty")) {
			firstName = req.getParameter("firstname");
			lastName = req.getParameter("lastname");
			username = req.getParameter("username");
			password = req.getParameter("psw");
			confirmPassword = req.getParameter("confirm_psw");
			email = req.getParameter("email");
			if (usernameExists(con, username)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp?err=1&"
						+ "fn="+firstName+"&ln="+lastName+"&un="+username+"&ea="+email);
				dispatch.forward(req, resp);
			} else if (emailExists(con, email)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp?err=2&"
						+ "fn="+firstName+"&ln="+lastName+"&un="+username+"&ea="+email);
				dispatch.forward(req, resp);
			} else if (!password.equals(confirmPassword)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp?err=3&"
						+ "fn="+firstName+"&ln="+lastName+"&un="+username+"&ea="+email);
				dispatch.forward(req, resp);
			} else {
				createNewAccount(con, firstName, lastName, username, password, email);
				RequestDispatcher dispatch = req.getRequestDispatcher("register_success.jsp");
				dispatch.forward(req, resp);
			}
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new RuntimeException(e);
		}
	}
	
	
}
