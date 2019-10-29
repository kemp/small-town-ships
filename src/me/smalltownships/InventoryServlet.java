package me.smalltownships;

import static me.smalltownships.Products.getAllProducts;
import static me.smalltownships.Products.findProductByID;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
		// Ensure the user is authenticated
		LoginHandler loginHandler = new LoginHandler();
		
		if (! loginHandler.isLoggedIn()) {
			// User is unauthorized, take them to the login page.
			response.sendRedirect("./");
			
			return;
		}
    	
    	// Append the list of products to the current request
    	request.setAttribute("inventorymanagement", getAllProducts());
 
        // Forward to /WEB-INF/views/products.jsp
        // (Users can not access directly into JSP pages placed in WEB-INF)
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/inventorymanagement.jsp");
        dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		int productId = Integer.parseInt(request.getParameter("productId"));
		int quantity = Integer.parseInt(request.getParameter("quantity"));
		
		Product p = Products.findProductByID(productId);
		
		if((p.getQuantity()- quantity) < 0)
		{
			quantity = 0;
		}
		
		//Compile the list of products
		Map<Product, Integer> productsCheckoutMap = new HashMap<Product, Integer>(); // <Product, quantity>
		
		productsCheckoutMap.put(Products.findProductByID(productId), quantity);
		
		TransactionHandler.createAdminTransaction(productsCheckoutMap);
		
    	// Append the list of products to the current request
    	request.setAttribute("inventorymanagement", getAllProducts());
 
        // Forward to /WEB-INF/views/products.jsp
        // (Users can not access directly into JSP pages placed in WEB-INF)
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/inventorymanagement.jsp");
        dispatcher.forward(request, response);
        
       
	}

}
