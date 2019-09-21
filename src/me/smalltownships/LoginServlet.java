package me.smalltownships;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		/*LoginHandler loginHandler = new LoginHandler();
				
		if (loginHandler.tryLogin(username, password)) {
			response.sendRedirect("products");
		} else {
			response.sendRedirect("/"); // Incorrect password
		}
		
		try {
			loginHandler.close();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Driver loaded");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smalltownships", "root", "123456789");
			System.out.println("Database connected");
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("select username, password from verifiedaccounts where username = '"+ username +"' AND password = '" + password+"'");
			
			if(rs.next())
			{
				response.sendRedirect("products");
			}
			else
			{
				response.sendRedirect("index");
			}
			conn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}