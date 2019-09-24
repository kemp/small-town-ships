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
	
	private boolean emailExists(MySQLHandler handler, String email) throws SQLException {
		String sql;
		ResultSet rs;
		boolean exists;
		// Check for matches in the verified users
		sql = "SELECT * FROM verifiedaccounts "
				+ "WHERE email = '" + email + "';";
		rs = handler.queryTable(sql);
		exists = rs.first();
		rs.close();
		if (exists) {
			// Match found
			return true;
		}
		// Check for matches in the unverified users
		sql = "SELECT * FROM unverifiedaccounts "
				+ "WHERE email = '" + email + "';";
		rs = handler.queryTable(sql);
		exists = rs.first();
		rs.close();
		return exists;	// Return if a match was found
	}

	private boolean userExists(MySQLHandler handler, String user) throws SQLException {
		String sql;
		ResultSet rs;
		boolean exists;
		// Check for matches in the verified users
		sql = "SELECT * FROM verifiedaccounts "
				+ "WHERE username = '" + user + "';";
		rs = handler.queryTable(sql);
		exists = rs.first();
		rs.close();
		if (exists) {
			// Match found
			return true;
		}
		// Check for matches in the unverified users
		sql = "SELECT * FROM unverifiedaccounts "
				+ "WHERE username = '" + user + "';";
		rs = handler.queryTable(sql);
		exists = rs.first();
		rs.close();
		return exists;	// Return if a match was found
	}
	
	private void createNewAccount(MySQLHandler handler, String firstName, String lastName,
			String user, String email, String password) throws SQLException {
		ResultSet rs;
		String sql;
		Date date;
		sql = "INSERT INTO unverifiedaccounts VALUES "
				+ "('"+firstName+"','"+lastName+"','"+user+"','"+password+"','"+email
				+ "', CURDATE());";
		handler.updateTable(sql);
		sql = "SELECT applicationDate FROM unverifiedaccounts WHERE "
				+ "username = '"+user+"';";
		rs = handler.queryTable(sql);
		rs.next();
		date = rs.getDate(1);
		rs.close();
		try {
			EmailVerifier.createVerificationPage(email, user, date.toString());
		} catch (Exception e) {
			sql = "DELETE FROM unverifiedaccounts WHERE username='"+user+"';";
			handler.updateTable(sql);
			throw new RuntimeException("Could not create verification email", e);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String firstName, lastName, username, email, password;
		
		try (MySQLHandler sqlHandler = new MySQLHandler()) {
			firstName = req.getParameter("FirstName");
			lastName = req.getParameter("LastName");
			username = req.getParameter("UserName");
			email = req.getParameter("inputEmail");
			password = req.getParameter("inputPassword");
			if (emailExists(sqlHandler, email) || userExists(sqlHandler, username)) {
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp");
				dispatch.forward(req, resp);	// FIXME: Better way to indicate an error?
			} else {
				createNewAccount(sqlHandler, firstName, lastName, username, email, password);
				RequestDispatcher dispatch = req.getRequestDispatcher("register_success.jsp");
				dispatch.forward(req, resp);
			}
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new RuntimeException(e);
		}
	}
	
	
}
