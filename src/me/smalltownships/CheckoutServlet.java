package me.smalltownships;

import static me.smalltownships.Products.getProducts;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.smalltownships.Products;
import me.smalltownships.Product;

/**
 * Servlet where users can check out
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Show the checkout page, with a detail of all the products to purchase.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		List<Product> products = getProducts();
		String productsJson = "{";
		
		for (Product p : products) {
			productsJson += "\\\"" + p.getId() + "\\\": \\\"" + p.getName() + "\\\",";
		}
		
		productsJson = productsJson.substring(0, productsJson.length() - 1); // Remove last comma
		
		productsJson += "}";
		
    	request.setAttribute("productNames", productsJson);
		
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/checkout.jsp");
        dispatcher.forward(request, response);
	}
	
	/**
	 * Process the checkout.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		
		if(! validateInputNotEmpty(request, Arrays.asList("products", "address", "cc-number", "cc-exp"))) {
			response.sendError(400); // TODO: Change the error number or show a more helpful message
			return;
		}
		
		String productsList = request.getParameter("products");
		String deliveryAddress = request.getParameter("address");
		String creditCardNumber = request.getParameter("cc-number");
		String creditCardExpiration = request.getParameter("cc-exp");
		
		// Compile the list of products
		Map<Product, Integer> productsCheckoutMap = new HashMap<Product, Integer>(); // <Product, quantity>
		
		for (String productTuple : productsList.split(";")) {
			if (productTuple.split(",").length != 2) {
				continue;
			}
			
			Integer productId = Integer.valueOf(productTuple.split(",")[0]);
			
			Integer productQuantity = Integer.valueOf(productTuple.split(",")[1]);
			
			productsCheckoutMap.put(Products.findProductByID(productId), productQuantity);
		}
		
		TransactionHandler.createTransaction(productsCheckoutMap, deliveryAddress, creditCardNumber, creditCardExpiration);
		
		
		// Show the user that the request has submitted successfully
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/WEB-INF/views/confirmation.jsp");
        dispatcher.forward(request, response);
	}
	
	private boolean validateInputNotEmpty (HttpServletRequest request, List<String> parameters) {
		for (String parameter : parameters) {
			String value = request.getParameter(parameter);
			
			if (value == null || value.trim().isEmpty()) {
				return false;
			}
		}
		
		return true;
	}

}