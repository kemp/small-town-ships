package me.smalltownships;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
public class InventoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InventoryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = User.loggedInUser(request.getSession());

		// Ensure the user is authenticated
		if (user == null) {
			// User is unauthenticated, take them to the login page.
			response.sendRedirect("./");

			return;
		}

		// Ensure the user is authorized to see this page
		if (! user.isAdmin()) {
            response.sendRedirect("./");

            return;
		}

    	// Append the list of products to the current request
    	request.setAttribute("inventorymanagement", Products.getAllProducts());
 
        // Forward to /WEB-INF/views/products.jsp
        // (Users can not access directly into JSP pages placed in WEB-INF)
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/inventorymanagement.jsp");
        dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = User.loggedInUser(request.getSession());

		// Ensure the user is authenticated
		if (user == null) {
			// User is unauthorized, take them to the login page.
			response.sendRedirect("/");

			return;
		}

		// Ensure the user is authorized to see this page
		if (! user.isAdmin()) {
			response.sendRedirect("/");

			return;
		}

		int productId = Integer.parseInt(request.getParameter("productId"));
		int quantity = Integer.parseInt(request.getParameter("quantity"));
		
		Product p = Products.findProductByID(productId);
		
		int quantityChange = quantity - p.getQuantity();
		
		if(quantity >= 0)
		{
			//Compile the list of products
			Map<Product, Integer> productsCheckoutMap = new HashMap<Product, Integer>(); // <Product, quantity>
			
			productsCheckoutMap.put(Products.findProductByID(productId), quantityChange);
			
			TransactionHandler.createAdminTransaction(productsCheckoutMap);
			
			request.setAttribute("me.smalltownships.InventoryServlet.success", "true");
		} else {
			request.setAttribute("me.smalltownships.InventoryServlet.error", "true");
		}
		
		
    	// Append the list of products to the current request
    	request.setAttribute("inventorymanagement", Products.getAllProducts());
 
        // Forward to /WEB-INF/views/products.jsp
        // (Users can not access directly into JSP pages placed in WEB-INF)
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/inventorymanagement.jsp");
        dispatcher.forward(request, response);
	}

}
