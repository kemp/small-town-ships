package me.smalltownships;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static me.smalltownships.Products.findProductByID;

/**
 * HttpServlet representing an individual Product
 * 
 * @author kemp
 */
@WebServlet("/product")
public class ProductsShowServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Show a given product detail.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Ensure the user is authenticated
		LoginHandler loginHandler = new LoginHandler();
		
		if (! loginHandler.isLoggedIn()) {
			// User is unauthorized, take them to the login page.
			response.sendRedirect("./");
			
			try {
				loginHandler.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		int id;
		
		try {
			id = Integer.parseInt((String) request.getParameter("id"));
		} catch (NumberFormatException e) {
			// The requested id was null or not a valid number
			response.sendError(404);
			return;
		}
				
		// Find the product by given ID
		Product product = findProductByID(id);
		
		// Make sure the product by that ID exists
		if (product == null) {
			response.sendError(404);
			return;
		}
		
		// Send the product to the view
		request.setAttribute("product", product);
		
        // Forward to /WEB-INF/views/product.jsp
        // (Users can not access directly into JSP pages placed in WEB-INF)
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/product.jsp");
        dispatcher.forward(request, response);
        
        try {
        	loginHandler.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
