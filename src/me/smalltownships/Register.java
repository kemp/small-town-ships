package me.smalltownships;

import java.io.IOException;
import java.sql.Date;
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

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find JDBC libraries", e);
		}
	}
	
	private boolean usernameExists(MySQLHandler handler, String user) throws SQLException {
		ResultSet rs;
		String sql;
		int cnt;
		// Check for matches in the verified users
		sql = "SELECT COUNT(username) FROM smalltownships.verifiedaccounts "
				+ "WHERE username = '" + user + "';";
		rs = handler.queryTable(sql);
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		if (cnt != 0) {
			// Match found
			return true;
		}
		// Check for matches in the unverified users
		sql = "SELECT COUNT(username) FROM smalltownships.unverifiedaccounts "
				+ "WHERE username = '" + user + "';";
		rs = handler.queryTable(sql);
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		return (cnt != 0);	// Return if a match was found
	}

	private boolean emailExists(MySQLHandler handler, String email) throws SQLException {
		String sql;
		ResultSet rs;
		int cnt;
		// Check for matches in the verified users
		sql = "SELECT COUNT(email) FROM smalltownships.verifiedaccounts "
				+ "WHERE email = '" + email + "';";
		rs = handler.queryTable(sql);
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		if (cnt != 0) {
			// Match found
			return true;
		}
		// Check for matches in the unverified users
		sql = "SELECT COUNT(email) FROM smalltownships.unverifiedaccounts "
				+ "WHERE email = '" + email + "';";
		rs = handler.queryTable(sql);
		cnt = (rs.first() ? rs.getInt(1) : 0);
		rs.close();
		return (cnt != 0);	// Return if a match was found
	}
	
	private void createNewAccount(MySQLHandler handler, String firstName, String lastName,
			String username, String password, String email) throws SQLException {
		ResultSet rs;
		String sql;
		Date date;
		sql = "INSERT INTO smalltownships.unverifiedaccounts VALUES "
				+ "('"+firstName+"','"+lastName+"','"+username+"','"+password+"','"+email
				+ "', CURDATE());";
		handler.updateTable(sql);
		sql = "SELECT applicationDate FROM smalltownships.unverifiedaccounts WHERE "
				+ "username = '"+username+"';";
		rs = handler.queryTable(sql);
		date = rs.getDate(1);
		rs.close();
		try {
			EmailVerifier.createVerificationPage(email, username, date.toString());
		} catch (IOException e) {
			throw new RuntimeException("Could not create verification page", e);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String firstName, lastName, username, password, confirmPassword, email;
		
		try (MySQLHandler sqlHandler = new MySQLHandler()) {
			firstName = req.getParameter("firstname");
			lastName = req.getParameter("lastname");
			username = req.getParameter("username");
			password = req.getParameter("psw");
			confirmPassword = req.getParameter("confirm_psw");
			email = req.getParameter("email");
			if (usernameExists(sqlHandler, username)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp?err=1&"
						+ "fn="+firstName+"&ln="+lastName+"&un="+username+"&ea="+email);
				dispatch.forward(req, resp);
			} else if (emailExists(sqlHandler, email)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp?err=2&"
						+ "fn="+firstName+"&ln="+lastName+"&un="+username+"&ea="+email);
				dispatch.forward(req, resp);
			} else if (!password.equals(confirmPassword)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp?err=3&"
						+ "fn="+firstName+"&ln="+lastName+"&un="+username+"&ea="+email);
				dispatch.forward(req, resp);
			} else {
				createNewAccount(sqlHandler, firstName, lastName, username, password, email);
				RequestDispatcher dispatch = req.getRequestDispatcher("register_success.jsp");
				dispatch.forward(req, resp);
			}
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new RuntimeException(e);
		}
	}
	
	
}
