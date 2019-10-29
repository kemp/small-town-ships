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
		
		LoginHandler loginHandler = new LoginHandler();
				
		if (loginHandler.tryLogin(username, password)) {
			response.sendRedirect("products");
		} else {
			// Incorrect username or password
			request.setAttribute("me.smalltownships.login.loginerr",
					"Incorrect username or password");
			request.getRequestDispatcher("index.jsp").forward(request, response);
		}
		
	}

}