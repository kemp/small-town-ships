package me.smalltownships;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		// Try to login the user
		User user = User.tryLogin(username, password, request.getSession());
		
		if (user != null) {
			response.sendRedirect("products");
		} else {
			// Incorrect username or password
			request.setAttribute("me.smalltownships.login.loginerr", Boolean.TRUE);
			request.getRequestDispatcher("index.jsp").forward(request, response);
		}
		
	}

}