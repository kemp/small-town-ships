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
	LoginHandler lh = new LoginHandler();
	
	private boolean emailExists(MySQLHandler handler, String email) throws SQLException {
		return lh.emailExists(email);
	}

	private boolean userExists(MySQLHandler handler, String user) throws SQLException {
		return lh.usernameExists(user);
	}
	
	private void createNewAccount(MySQLHandler handler, String firstName, String lastName,
			String user, String email, String password) throws SQLException {
		ResultSet rs;
		Date date;
		rs = handler.callProcedure("Create_Unverified_User(?,?,?,?,?)", 5, 
				new String[] {firstName, lastName, user, password, email});
		rs.next();
		date = rs.getDate("applicationDate");
		rs.close();
		try {
			EmailVerifier.createVerificationPage(email, user, date.toString());
		} catch (Exception e) {
			handler.callProcedure("Remove_User(?)", 1, new String[] {user});
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
