package me.smalltownships;

import static me.smalltownships.Products.getProducts;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Inventory
 */
@WebServlet("/inventory")
public class Inventory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Inventory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Ensure the user is authenticated
				LoginHandler loginHandler = new LoginHandler();
				
				if (! loginHandler.isLoggedIn()) {
					// User is unauthorized, take them to the login page.
					response.sendRedirect("./");
					
					return;
				}
		    	
		    	// Append the list of products to the current request
		    	request.setAttribute("inventorymanagement", getProducts());
		 
		        // Forward to /WEB-INF/views/products.jsp
		        // (Users can not access directly into JSP pages placed in WEB-INF)
		        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/inventorymanagement.jsp");
		        dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String number = request.getParameter("count");
		
		System.out.println(number);
		System.out.println(number);
		System.out.println(number);
	}

}
