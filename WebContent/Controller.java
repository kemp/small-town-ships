package smalltownshipsservlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.*;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/Controller")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Controller() {
        super();
        // TODO Auto-generated constructor stub
    }

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
		
		try
		{
			if(username == null)
			{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306","root","");
				String Query = "select * from verifiedaccounts where username=? and password=?";
				PreparedStatement psm = conn.prepareStatement(Query);
				psm.setString(1, username);
				psm.setString(2, password);
				
				ResultSet rs =psm.executeQuery();
				
				if(rs.next())
				{
					response.sendRedirect("Products.jsp");
				}
				else
				{
					System.out.println("Login Failed");
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println("Exception" + ex.getMessage());
		}
	}

}
