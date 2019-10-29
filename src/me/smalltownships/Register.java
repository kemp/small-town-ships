package me.smalltownships;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Pattern USERNAME_REGEX = Pattern.compile("[a-zA-Z0-9_]+");
	// don't really need much checking since we send them an email to verify the address anyways
	private static final Pattern EMAIL_REGEX = Pattern.compile("[^@]+@.+");
	private static final Pattern PASSWORD_REGEX = Pattern.compile(
			"[\\w!@#\\$%\\^\\&\\*\\-=\\+\\|\\\\\\?/]+");
	
	LoginHandler lh = new LoginHandler();
	
	private String emailExists(MySQLHandler handler, String email) throws SQLException {
		return lh.emailExists(email) ? "Email already exists!" : null;
	}

	private String userExists(MySQLHandler handler, String user) throws SQLException {
		return lh.usernameExists(user) ? "Username already exists!" : null;
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
	
	private String validateFirstNameInput(String firstname) {
		if (firstname == null || firstname.length() == 0) {
			return "First Name Required!";
		} else if (firstname.length() > 20) {
			return "First Name must be no more than 20 characters!";
		}
		return null;
	}

	private String validateLastNameInput(String lastname) {
		if (lastname == null || lastname.length() == 0) {
			return "Last Name Required!";
		} else if (lastname.length() > 30) {
			return "First Name must be no more than 30 characters!";
		}
		return null;
	}
	
	private String validateUsernameInput(String username) {
		if (username == null || username.length() == 0) {
			return "User Name Required!";
		} else if (username.length() > 20) {
			return "User Name must be no more than 20 characters!";
		} else if (!USERNAME_REGEX.matcher(username).matches()) {
			return "User Name must consist of only letters, numbers and underscores!";
		}
		return null;
	}
	
	private String validateEmailInput(String email) {
		if (email == null || email.length() == 0) {
			return "Email Required!";
		} else if (email.length() > 60) {
			return "Email must be no more than 60 characters!";
		} else if (!EMAIL_REGEX.matcher(email).matches()) {
			return "Invalid Email Address!";
		}
		return null;
	}
	
	private String validatePasswordInput(String password) {
		if (password == null || password.length() == 0) {
			return "Password Required!";
		} else if (password.length() < 4 || password.length() > 25) {
			return "Password must be between 4 and 25 characters long";
		} else if (!PASSWORD_REGEX.matcher(password).matches()) {
			return "Password must consist of only letters, numbers, and the special characters !@#$%^&*-=+|\\?/";
		}
		return null;
	}
	
	private boolean validateInputs(String fn, String ln, String un, String em, String pw,
			HttpServletRequest req) {
		String msg;
		boolean err = false;
		msg = validateFirstNameInput(fn);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.fnerrmsg", msg);
			err = true;
		}
		msg = validateLastNameInput(ln);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.lnerrmsg", msg);
			err = true;
		}
		msg = validateUsernameInput(un);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.unerrmsg", msg);
			err = true;
		}
		msg = validateEmailInput(em);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.emerrmsg", msg);
			err = true;
		}
		msg = validatePasswordInput(pw);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.pwerrmsg", msg);
			err = true;
		}
		return err;
	}
	
	private boolean userOrEmailExists(String un, String em, MySQLHandler handler,
			HttpServletRequest req) throws SQLException {
		String msg;
		boolean err = false;
		msg = userExists(handler, un);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.unerrmsg", msg);
			err = true;
		}
		msg = emailExists(handler, em);
		if (msg != null) {
			req.setAttribute("me.smalltownships.Register.emerrmsg", msg);
			err = true;
		}
		return err;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String firstName, lastName, username, email, password;
		String msg = "";
		firstName = req.getParameter("FirstName");
		lastName = req.getParameter("LastName");
		username = req.getParameter("UserName");
		email = req.getParameter("inputEmail");
		password = req.getParameter("inputPassword");
		
		// validate input
		if (validateInputs(firstName, lastName, username, email, password, req)) {
			// send error message if any validation failed
			req.setAttribute("me.smalltownships.Register.err", Boolean.TRUE);
			RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp");
			dispatch.forward(req, resp);
			return;
		}
		
		try (MySQLHandler sqlHandler = new MySQLHandler()) {
			if (userOrEmailExists(username, email, sqlHandler, req)) {
				req.setAttribute("me.smalltownships.Register.err", Boolean.TRUE);
				RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp");
				dispatch.forward(req, resp);
			} else {
				createNewAccount(sqlHandler, firstName, lastName, username, email, password);
				RequestDispatcher dispatch = req.getRequestDispatcher("register_success.jsp");
				dispatch.forward(req, resp);
			}
		} catch (Exception e) {
			// account could not be created
			msg = "Account could not be created due to the following error: " + e;
			req.setAttribute("me.smalltownships.Register.errmsg", msg);
			req.setAttribute("me.smalltownships.Register.err", Boolean.TRUE);
			RequestDispatcher dispatch = req.getRequestDispatcher("register.jsp");
			dispatch.forward(req, resp);
		}
	}
	
	
}
