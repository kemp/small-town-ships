package me.smalltownships;

import java.io.IOException;

import javax.servlet.http.*;  
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;

import static me.smalltownships.Products.getProducts;

/**
 * HttpServlet representing a list of products
 * 
 * @author kemp
 */
@WebServlet("/products")
public class ProductsIndexServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
    /**
     * Show a list of all products.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
		// Ensure the user is authenticated
		if (! User.isLoggedIn(request.getSession())) {
			// User is unauthorized, take them to the login page.
			response.sendRedirect("./");
			
			return;
		}
    	
    	// Append the list of products to the current request
    	request.setAttribute("products", getProducts());
 
        // Forward to /WEB-INF/views/products.jsp
        // (Users can not access directly into JSP pages placed in WEB-INF)
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/products.jsp");
        dispatcher.forward(request, response);
    }

}
